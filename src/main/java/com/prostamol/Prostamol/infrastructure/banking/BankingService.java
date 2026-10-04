package com.prostamol.Prostamol.infrastructure.banking;

import com.prostamol.Prostamol.domain.model.account.*;
import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.model.transaction.*;
import com.prostamol.Prostamol.domain.port.out.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.support.TransactionTemplate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;

public class BankingService {
    private final BankingProviderPort provider;
    private final BankConnectionRepository connections;
    private final BankAccountLinkRepository links;
    private final ImportedBankEntryRepository receipts;
    private final AccountRepositoryPort accounts;
    private final TransactionRepositoryPort transactions;
    private final TransactionTemplate tx;
    private final int historyDays;
    private final jakarta.persistence.EntityManager entityManager;

    public record Started(UUID connectionId, String url) {}
    public record AccountView(UUID accountId, String currency, java.math.BigDecimal bookedBalance, Instant balanceUpdatedAt) {}
    public record ConnectionView(UUID id, String bankName, String country, String status, Instant validUntil,
        Instant lastSyncedAt, String lastError, int skippedTransactions, List<AccountView> accounts) {}

    public BankingService(BankingProviderPort provider, BankConnectionRepository connections,
        BankAccountLinkRepository links, ImportedBankEntryRepository receipts, AccountRepositoryPort accounts,
        TransactionRepositoryPort transactions, TransactionTemplate tx, int historyDays,
        jakarta.persistence.EntityManager entityManager) {
        this.provider = provider; this.connections = connections; this.links = links; this.receipts = receipts;
        this.accounts = accounts; this.transactions = transactions; this.tx = tx; this.historyDays = historyDays;
        this.entityManager = entityManager;
    }

    public List<BankingProviderPort.Bank> banks(String country) { return provider.banks(country); }

    public Started start(UUID userId, String bankName, String country, String psuType) {
        var bank = provider.banks(country).stream().filter(b -> b.name().equals(bankName)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Bank not available in selected country"));
        if (bank.maximumConsentValidity() <= 0) throw new IllegalArgumentException("Bank does not support reusable consent");
        String state = UUID.randomUUID().toString() + UUID.randomUUID();
        BankConnection c = new BankConnection();
        c.id = UUID.randomUUID(); c.userId = userId; c.bankName = bank.name(); c.country = country;
        c.stateHash = hash(state); c.stateExpiresAt = Instant.now().plusSeconds(1800);
        c.status = "PENDING"; c.importFrom = LocalDate.now(ZoneOffset.UTC).minusDays(historyDays);
        c.validUntil = Instant.now().plusSeconds(Math.min(bank.maximumConsentValidity(), 180L * 86400));
        var authorization = provider.authorize(bank, psuType, state, userId.toString(), c.validUntil);
        connections.save(c);
        return new Started(c.id, authorization.url());
    }

    public ConnectionView complete(UUID userId, String state, String code, String error) {
        BankConnection found = connections.findByStateHash(hash(state))
            .orElseThrow(() -> new IllegalArgumentException("Invalid or already used banking state"));
        // Consume state in a separate committed transaction before exchanging the one-time code.
        UUID id = tx.execute(s -> {
            BankConnection c = ownedLocked(userId, found.id);
            if (!"PENDING".equals(c.status) || c.stateHash == null || !c.stateExpiresAt.isAfter(Instant.now()))
                throw new IllegalArgumentException("Banking authorization expired or already used; connect again");
            c.stateHash = null;
            c.status = error == null ? "CONNECTING" : "CANCELLED";
            connections.save(c);
            return c.id;
        });
        if (error != null) return view(connections.findById(id).orElseThrow());
        BankingProviderPort.Session session;
        try {
            session = provider.exchange(code);
        } catch (RuntimeException ex) {
            fail(id, "FAILED", "Authorization could not be completed; connect again");
            throw new BankingException(502, "Bank authorization could not be completed; connect again");
        }
        try {
            tx.executeWithoutResult(s -> {
                BankConnection c = ownedLocked(userId, id);
                if (!"CONNECTING".equals(c.status)) throw new BankingException(409, "Connection was cancelled");
                c.sessionId = session.id(); c.validUntil = session.validUntil(); c.status = "ACTIVE";
                for (var remote : session.accounts().stream().sorted(Comparator.comparing(BankingProviderPort.RemoteAccount::identificationHash)).toList()) {
                    String identity = hash(remote.identificationHash());
                    UUID accountId = stableId(userId + ":" + identity + ":" + remote.currency());
                    BankAccountLink link = links.lockById(accountId).orElseGet(BankAccountLink::new);
                    if (link.accountId != null) entityManager.refresh(link, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
                    if (link.accountId == null) {
                        link.accountId = accountId; link.userId = userId; link.identityHash = identity; link.currency = remote.currency();
                        accounts.save(new Account(accountId, userId, remote.name().substring(0, Math.min(255, remote.name().length())),
                            AccountType.BANK_ACCOUNT, Money.zero(remote.currency())));
                    } else if (accounts.findById(accountId).isEmpty()) {
                        // Account deleted by user: do not silently recreate it or its history.
                        continue;
                    }
                    link.connectionId = id; link.remoteUid = remote.uid(); links.save(link);
                }
                connections.save(c);
            });
        } catch (RuntimeException ex) {
            try { provider.disconnect(session.id()); }
            catch (RuntimeException ignored) { /* Persist the session below so disconnect can be retried. */ }
            tx.executeWithoutResult(s -> {
                BankConnection c = ownedLocked(userId, id);
                c.sessionId = session.id(); connections.save(c);
            });
            fail(id, "FAILED", "Authorization could not be completed; connect again");
            throw new BankingException(502, "Bank authorization could not be completed; connect again");
        }
        // Session and accounts survive a failed initial data fetch, allowing a manual retry.
        return sync(userId, id);
    }

    public List<ConnectionView> list(UUID userId) { return connections.findAllByUserId(userId).stream().map(this::view).toList(); }

    public ConnectionView sync(UUID userId, UUID id) {
        // Authorize before catch/failure recording, so strangers cannot mutate connection metadata.
        BankConnection existing = connections.findById(id).orElseThrow(() -> new IllegalArgumentException("Connection not found"));
        requireOwner(userId, existing);
        try {
            tx.executeWithoutResult(s -> {
                BankConnection c = ownedLocked(userId, id);
                if (!"ACTIVE".equals(c.status)) throw new BankingException(409, "Connection is not active; connect again");
                if (!c.validUntil.isAfter(Instant.now())) {
                    c.status = "EXPIRED"; connections.save(c); return;
                }
                int skipped = 0;
                LocalDate to = LocalDate.now(ZoneOffset.UTC);
                for (BankAccountLink link : links.lockAllByConnectionId(id)) {
                    entityManager.refresh(link, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
                    if (!id.equals(link.connectionId)) continue;
                    var local = accounts.findById(link.accountId);
                    if (local.isEmpty()) continue;
                    if (!local.get().getInitialBalance().currency().equals(link.currency))
                        throw new BankingException(409, "Linked account currency changed; restore its original currency before syncing");
                    String continuation = null;
                    Set<String> seen = new HashSet<>();
                    int pages = 0;
                    do {
                        if (++pages > 1000) throw new BankingException(502, "Bank returned too many transaction pages");
                        var page = provider.transactions(link.remoteUid, c.importFrom, to, continuation);
                        skipped += page.skipped();
                        for (var entry : page.entries()) {
                            UUID entryId = stableId(link.accountId + ":" + entry.reference());
                            if (receipts.existsById(entryId)) continue;
                            Money.zero(link.currency).assertSameCurrency(entry.amount());
                            transactions.save(new Transaction(entryId, userId, link.accountId,
                                entry.credit() ? TransactionType.INCOME : TransactionType.EXPENSE,
                                entry.amount(), entry.date(), entry.description(), null, null, false, null));
                            receipts.save(new ImportedBankEntry(entryId));
                        }
                        continuation = page.continuationKey();
                        if (continuation != null && !seen.add(continuation)) throw new BankingException(502, "Bank repeated a transaction page");
                    } while (continuation != null);
                    Money balance = provider.balance(link.remoteUid, link.currency);
                    link.bookedBalance = balance == null ? null : balance.amount();
                    link.balanceUpdatedAt = balance == null ? null : Instant.now();
                    links.save(link);
                }
                c.lastSyncedAt = Instant.now(); c.lastError = null; c.skippedTransactions = skipped;
                connections.save(c);
            });
        } catch (RuntimeException ex) {
            if (ex instanceof EnableBankingException providerError) {
                fail(id, providerError.connectionStatus(), providerError.getMessage());
            } else {
                fail(id, null, "Sync failed; retry or reconnect if bank consent was revoked");
            }
            if (ex instanceof BankingException b) throw b;
            throw new BankingException(502, "Bank sync failed; no partial transactions were imported");
        }
        return view(connections.findById(id).orElseThrow());
    }

    public void disconnect(UUID userId, UUID id) {
        tx.executeWithoutResult(s -> {
            BankConnection c = ownedLocked(userId, id);
            if ("DISCONNECTED".equals(c.status) && c.sessionId == null) return;
            if (c.sessionId != null) {
                try {
                    provider.disconnect(c.sessionId);
                } catch (EnableBankingException ex) {
                    if (!ex.sessionEnded()) {
                        throw ex;
                    }
                }
            }
            c.status = "DISCONNECTED"; c.stateHash = null; c.sessionId = null; c.lastError = null;
            connections.save(c);
        });
    }

    public void syncDue() {
        for (BankConnection c : connections.findAllByStatus("ACTIVE")) {
            if (c.lastSyncedAt != null && c.lastSyncedAt.isAfter(Instant.now().minusSeconds(6 * 3600))) continue;
            try { sync(c.userId, c.id); }
            catch (RuntimeException ignored) { /* Safe error is persisted; continue with other connections. */ }
        }
    }
    private BankConnection ownedLocked(UUID userId, UUID id) {
        BankConnection c = connections.lockById(id).orElseThrow(() -> new IllegalArgumentException("Connection not found"));
        // Open-in-view may already have loaded the row before another request acquired the lock.
        entityManager.refresh(c, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        requireOwner(userId, c); return c;
    }
    private static void requireOwner(UUID userId, BankConnection c) {
        if (!c.userId.equals(userId)) throw new AccessDeniedException("Connection does not belong to the authenticated user");
    }
    private void fail(UUID id, String status, String message) {
        tx.executeWithoutResult(s -> {
            BankConnection c = connections.lockById(id).orElseThrow();
            entityManager.refresh(c, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
            if (status != null && !"DISCONNECTED".equals(c.status)) c.status = status;
            c.lastError = message; connections.save(c);
        });
    }
    private ConnectionView view(BankConnection c) {
        String status = "ACTIVE".equals(c.status) && !c.validUntil.isAfter(Instant.now()) ? "EXPIRED" : c.status;
        return new ConnectionView(c.id, c.bankName, c.country, status, c.validUntil, c.lastSyncedAt, c.lastError,
            c.skippedTransactions, links.findAllByConnectionIdOrderByAccountId(c.id).stream()
                .map(a -> new AccountView(a.accountId, a.currency, a.bookedBalance, a.balanceUpdatedAt)).toList());
    }
    static UUID stableId(String value) { return UUID.nameUUIDFromBytes(("enable-banking:" + value).getBytes(StandardCharsets.UTF_8)); }
    static String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
}
