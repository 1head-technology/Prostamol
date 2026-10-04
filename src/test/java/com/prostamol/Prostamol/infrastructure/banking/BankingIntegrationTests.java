package com.prostamol.Prostamol.infrastructure.banking;

import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.port.out.*;
import com.prostamol.Prostamol.domain.port.in.account.GetAccountBalanceUseCase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.access.AccessDeniedException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:banking;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
    "spring.datasource.password=", "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop", "enable-banking.enabled=true", "enable-banking.sync-enabled=false"
})
@AutoConfigureMockMvc
class BankingIntegrationTests {
    @MockitoBean BankingProviderPort provider;
    @Autowired BankingService service;
    @Autowired BankConnectionRepository connections;
    @Autowired BankAccountLinkRepository links;
    @Autowired ImportedBankEntryRepository receipts;
    @Autowired AccountRepositoryPort accounts;
    @Autowired TransactionRepositoryPort transactions;
    @Autowired GetAccountBalanceUseCase balance;
    @Autowired MockMvc mvc;
    @Autowired com.prostamol.Prostamol.infrastructure.security.JwtService jwt;
    UUID user;
    String state;

    @BeforeEach void setup() {
        user = UUID.randomUUID();
        when(provider.banks("IT")).thenReturn(List.of(new BankingProviderPort.Bank("Test Bank", "IT", 86400)));
        when(provider.authorize(any(), anyString(), anyString(), anyString(), any())).thenAnswer(call -> {
            state = call.getArgument(2);
            return new BankingProviderPort.Authorization("https://auth.enablebanking.com/test");
        });
        when(provider.exchange(anyString())).thenReturn(new BankingProviderPort.Session("session", Instant.now().plusSeconds(3600),
            List.of(new BankingProviderPort.RemoteAccount("remote", "stable-account", "Test account", "EUR"))));
        when(provider.transactions(anyString(), any(), any(), isNull())).thenReturn(new BankingProviderPort.Page(List.of(entry("one")), "next+/=", 1));
        when(provider.transactions(anyString(), any(), any(), eq("next+/="))).thenReturn(new BankingProviderPort.Page(List.of(entry("two")), null, 0));
        when(provider.balance(anyString(), eq("EUR"))).thenReturn(new BankingProviderPort.Balances(Money.of(new BigDecimal("-23.50"), "EUR"), null));
    }
    BankingProviderPort.Entry entry(String ref) {
        return new BankingProviderPort.Entry(ref, Money.of(new BigDecimal("10.50"), "EUR"), false, LocalDate.now(), "Purchase");
    }
    BankingService.ConnectionView connect() {
        service.start(user, "Test Bank", "IT", "personal");
        return service.complete(user, state, "code", null);
    }

    @Test void importsAllPagesAndRepeatedSyncAndReconnectionDoNotDuplicate() {
        var c = connect();
        UUID accountId = c.accounts().getFirst().accountId();
        assertEquals(2, transactions.findAllByUserId(user).size());
        assertEquals(1, c.skippedTransactions());
        assertEquals(0, accounts.findById(accountId).orElseThrow().getInitialBalance().amount().signum());
        assertEquals(0, balance.execute(user, accountId).amount().compareTo(new BigDecimal("-23.50")));
        service.sync(user, c.id());
        connect();
        assertEquals(1, accounts.findAllByUserId(user).size());
        assertEquals(2, transactions.findAllByUserId(user).size());
        assertThrows(IllegalArgumentException.class, () -> service.complete(user, state, "code", null));
    }

    @Test void ownershipAndAnonymousAccessAreEnforced() throws Exception {
        var started = service.start(user, "Test Bank", "IT", "personal");
        assertThrows(AccessDeniedException.class, () -> service.complete(UUID.randomUUID(), state, "code", null));
        assertThrows(AccessDeniedException.class, () -> service.sync(UUID.randomUUID(), started.connectionId()));
        assertThrows(AccessDeniedException.class, () -> service.disconnect(UUID.randomUUID(), started.connectionId()));
        assertEquals("PENDING", connections.findById(started.connectionId()).orElseThrow().status);
        verify(provider, never()).exchange(anyString());
        mvc.perform(get("/api/v1/banking/connections")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/banking/connections/complete").contentType("application/json")
            .content("{\"state\":\"x\",\"code\":\"y\"}")).andExpect(status().isUnauthorized());
    }

    @Test void expiredStateAndCancellationNeverExchangeCode() {
        var started = service.start(user, "Test Bank", "IT", "personal");
        var c = connections.findById(started.connectionId()).orElseThrow();
        c.stateExpiresAt = Instant.now().minusSeconds(1); connections.save(c);
        assertThrows(IllegalArgumentException.class, () -> service.complete(user, state, "code", null));
        service.start(user, "Test Bank", "IT", "personal");
        assertEquals("CANCELLED", service.complete(user, state, null, "access_denied").status());
        verify(provider, never()).exchange(anyString());
    }

    @Test void failedPageRollsBackImportButSessionCanBeRetried() {
        when(provider.transactions(anyString(), any(), any(), eq("next+/="))).thenThrow(new BankingException(502, "Unavailable"));
        var started = service.start(user, "Test Bank", "IT", "personal");
        assertThrows(BankingException.class, () -> service.complete(user, state, "code", null));
        assertEquals(0, transactions.findAllByUserId(user).size());
        var saved = connections.findById(started.connectionId()).orElseThrow();
        assertEquals("ACTIVE", saved.status);
        assertNotNull(saved.sessionId);
        assertNotNull(saved.lastError);
        assertNull(saved.lastSyncedAt);
        doReturn(new BankingProviderPort.Page(List.of(entry("two")), null, 0))
            .when(provider).transactions(anyString(), any(), any(), eq("next+/="));
        service.sync(user, saved.id);
        assertEquals(2, transactions.findAllByUserId(user).size());
    }

    @Test void duplicatePagesAbortAndDeletedTransactionsStayDeleted() {
        var c = connect();
        var transaction = transactions.findAllByUserId(user).getFirst();
        transactions.deleteById(transaction.getId());
        service.sync(user, c.id());
        assertEquals(1, transactions.findAllByUserId(user).size());
        when(provider.transactions(anyString(), any(), any(), eq("next+/=")))
            .thenReturn(new BankingProviderPort.Page(List.of(entry("three")), "next+/=", 0));
        assertThrows(BankingException.class, () -> service.sync(user, c.id()));
        assertEquals(1, transactions.findAllByUserId(user).size());
    }

    @Test void disconnectRetainsDataAndExpiryStopsFetching() {
        var c = connect();
        var saved = connections.findById(c.id()).orElseThrow();
        saved.validUntil = Instant.now().minusSeconds(1); connections.save(saved);
        clearInvocations(provider);
        assertEquals("EXPIRED", service.sync(user, c.id()).status());
        verify(provider, never()).transactions(anyString(), any(), any(), any());
        service.disconnect(user, c.id());
        verify(provider).disconnect("session");
        assertEquals("DISCONNECTED", service.list(user).getFirst().status());
        assertEquals(2, transactions.findAllByUserId(user).size());
        assertThrows(BankingException.class, () -> service.sync(user, c.id()));
    }

    @Test void simultaneousSyncsDoNotDuplicate() throws Exception {
        var c = connect();
        when(provider.transactions(anyString(), any(), any(), isNull()))
            .thenReturn(new BankingProviderPort.Page(List.of(entry("new")), null, 0));
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> service.sync(user, c.id()));
            var second = executor.submit(() -> service.sync(user, c.id()));
            first.get(10, TimeUnit.SECONDS); second.get(10, TimeUnit.SECONDS);
        }
        assertEquals(3, transactions.findAllByUserId(user).size());
    }

    @Test void browserCallbackFlowValidatesRequestsAndDoesNotExposeSessionSecrets() throws Exception {
        String bearer = "Bearer " + jwt.generateToken(user, "bank-test@example.com", com.prostamol.Prostamol.domain.model.user.Role.USER);
        mvc.perform(get("/api/v1/banking/banks?country=invalid").header("Authorization", bearer))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/banking/connections").header("Authorization", bearer).contentType("application/json")
            .content("{\"bankName\":\"Test Bank\",\"country\":\"IT\",\"psuType\":\"personal\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.url").exists());
        mvc.perform(post("/api/v1/banking/connections/complete").header("Authorization", bearer).contentType("application/json")
            .content(new tools.jackson.databind.ObjectMapper().writeValueAsString(Map.of("state", state, "code", "code"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.accounts[0].accountId").exists())
            .andExpect(jsonPath("$.sessionId").doesNotExist()).andExpect(jsonPath("$.stateHash").doesNotExist());
        mvc.perform(post("/api/v1/banking/connections/complete").header("Authorization", bearer).contentType("application/json")
            .content(new tools.jackson.databind.ObjectMapper().writeValueAsString(Map.of("state", state, "code", "code"))))
            .andExpect(status().isBadRequest());
    }

    @Test void availableOnlyBalanceReachesConnectionAndAccountEndpointsAndClearsStaleSnapshots() throws Exception {
        var c = connect();
        var available = new BankingProviderPort.Balances(null, Money.of(new BigDecimal("42.50"), "EUR"));
        when(provider.balance(anyString(), eq("EUR"))).thenReturn(available);
        String bearer = "Bearer " + jwt.generateToken(user, "bank-test@example.com", com.prostamol.Prostamol.domain.model.user.Role.USER);
        mvc.perform(post("/api/v1/banking/connections/" + c.id() + "/sync").header("Authorization", bearer))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accounts[0].bookedBalance").isEmpty())
            .andExpect(jsonPath("$.accounts[0].availableBalance").value(42.50))
            .andExpect(jsonPath("$.accounts[0].balance").value(42.50))
            .andExpect(jsonPath("$.accounts[0].balanceUpdatedAt").isNotEmpty());
        UUID accountId = c.accounts().getFirst().accountId();
        mvc.perform(get("/api/v1/accounts/" + accountId + "/balance").header("Authorization", bearer))
            .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(42.50));
        var saved = links.findById(accountId).orElseThrow();
        assertNull(saved.bookedBalance);
        assertEquals(0, saved.availableBalance.compareTo(new BigDecimal("42.50")));

        when(provider.balance(anyString(), eq("EUR"))).thenThrow(new BankingException(502, "Unavailable"));
        assertThrows(BankingException.class, () -> service.sync(user, c.id()));
        assertEquals(0, balance.execute(user, accountId).amount().compareTo(new BigDecimal("42.50")));

        doReturn(new BankingProviderPort.Balances(Money.of(new BigDecimal("50"), "EUR"),
            Money.of(new BigDecimal("40"), "EUR"))).when(provider).balance(anyString(), eq("EUR"));
        assertEquals(0, service.sync(user, c.id()).accounts().getFirst().balance().compareTo(new BigDecimal("50")));
        assertEquals(0, balance.execute(user, accountId).amount().compareTo(new BigDecimal("50")));

        when(provider.balance(anyString(), eq("EUR"))).thenReturn(null);
        var missing = service.sync(user, c.id()).accounts().getFirst();
        assertNull(missing.bookedBalance());
        assertNull(missing.availableBalance());
        assertNull(missing.balance());
        assertNull(missing.balanceUpdatedAt());
        assertEquals(409, assertThrows(BankingException.class, () -> balance.execute(user, accountId)).status());
    }

    @Test void missingBankBalanceDoesNotReturnPartialHistoryAsBalance() {
        when(provider.balance(anyString(), eq("EUR"))).thenReturn(null);
        var c = connect();
        assertNull(c.accounts().getFirst().bookedBalance());
        assertThrows(BankingException.class, () -> balance.execute(user, c.accounts().getFirst().accountId()));
    }
    @Test void providerExpiryRollsBackSyncAndStopsScheduledRetries() {
        when(provider.exchange(anyString())).thenReturn(new BankingProviderPort.Session("session", Instant.now().plusSeconds(3600),
            List.of(new BankingProviderPort.RemoteAccount("expired-remote", "stable-account", "Test account", "EUR"))));
        var c = connect();
        var previousSync = c.lastSyncedAt();
        when(provider.transactions(anyString(), any(), any(), isNull()))
            .thenReturn(new BankingProviderPort.Page(List.of(entry("new-before-expiry")), "next+/=", 0));
        when(provider.transactions(anyString(), any(), any(), eq("next+/=")))
            .thenThrow(EnableBankingException.fromResponse(401, "{\"error\":\"EXPIRED_SESSION\"}"));
        assertThrows(BankingException.class, () -> service.sync(user, c.id()));
        var saved = connections.findById(c.id()).orElseThrow();
        assertEquals("EXPIRED", saved.status);
        assertTrue(saved.lastError.contains("EXPIRED_SESSION"));
        assertEquals(previousSync, service.list(user).getFirst().lastSyncedAt());
        assertEquals(2, transactions.findAllByUserId(user).size());
        assertEquals(0, new BigDecimal("-23.50").compareTo(balance.execute(user, c.accounts().getFirst().accountId()).amount()));
        clearInvocations(provider);
        service.syncDue();
        verify(provider, never()).transactions(eq("expired-remote"), any(), any(), any());
    }

    @Test void otherUnauthorizedErrorsDoNotExpireTheConnection() {
        var c = connect();
        when(provider.transactions(anyString(), any(), any(), isNull()))
            .thenThrow(EnableBankingException.fromResponse(401, "{\"error\":\"UNAUTHORIZED_ACCESS\"}"));
        assertThrows(BankingException.class, () -> service.sync(user, c.id()));
        var saved = connections.findById(c.id()).orElseThrow();
        assertEquals("ACTIVE", saved.status);
        assertTrue(saved.lastError.contains("UNAUTHORIZED_ACCESS"));
    }

    @Test void oneMissingBalanceDoesNotPreventOtherAccountBalancesFromSyncing() {
        when(provider.exchange(anyString())).thenReturn(new BankingProviderPort.Session("session", Instant.now().plusSeconds(3600),
            List.of(new BankingProviderPort.RemoteAccount("remote", "stable-account", "Test account", "EUR"),
                new BankingProviderPort.RemoteAccount("no-balance", "second-account", "Other account", "EUR"))));
        when(provider.balance("no-balance", "EUR")).thenReturn(null);
        var c = connect();
        assertEquals(2, c.accounts().size());
        assertEquals(1, c.accounts().stream().filter(a -> a.bookedBalance() != null).count());
        assertNull(c.lastError());
        for (var account : c.accounts()) {
            if (account.bookedBalance() == null) {
                assertEquals(409, assertThrows(BankingException.class,
                    () -> balance.execute(user, account.accountId())).status());
            } else {
                assertEquals(0, new BigDecimal("-23.50").compareTo(balance.execute(user, account.accountId()).amount()));
            }
        }
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
        "EXPIRED_SESSION", "CLOSED_SESSION", "REVOKED_SESSION", "SESSION_DOES_NOT_EXIST"
    })
    void disconnectCompletesWhenProviderSessionAlreadyEnded(String code) {
        var c = connect();
        var saved = connections.findById(c.id()).orElseThrow();
        saved.status = "EXPIRED";
        saved.lastError = "Previous sync failed";
        connections.save(saved);
        doThrow(EnableBankingException.fromResponse(401, "{\"error\":\"" + code + "\"}"))
            .when(provider).disconnect("session");

        service.disconnect(user, c.id());
        saved = connections.findById(c.id()).orElseThrow();
        assertEquals("DISCONNECTED", saved.status);
        assertNull(saved.sessionId);
        assertNull(saved.stateHash);
        assertNull(saved.lastError);
        assertEquals(2, transactions.findAllByUserId(user).size());
        assertTrue(accounts.findById(c.accounts().getFirst().accountId()).isPresent());
        assertEquals(0, new BigDecimal("-23.50").compareTo(balance.execute(user, c.accounts().getFirst().accountId()).amount()));

        service.disconnect(user, c.id());
        verify(provider, times(1)).disconnect("session");
    }

    @Test void disconnectKeepsSessionForRetryOnOtherProviderFailures() {
        var c = connect();
        for (BankingException error : List.of(
            EnableBankingException.fromResponse(401, "{\"error\":\"UNAUTHORIZED_ACCESS\"}"),
            EnableBankingException.fromResponse(401, "{}"),
            EnableBankingException.fromResponse(500, "{\"error\":\"ASPSP_ERROR\"}"),
            new BankingException(502, "Enable Banking unavailable")
        )) {
            doThrow(error).when(provider).disconnect("session");
            assertSame(error, assertThrows(BankingException.class, () -> service.disconnect(user, c.id())));
            var saved = connections.findById(c.id()).orElseThrow();
            assertEquals("ACTIVE", saved.status);
            assertEquals("session", saved.sessionId);
        }
        doNothing().when(provider).disconnect("session");
        service.disconnect(user, c.id());
        assertEquals("DISCONNECTED", connections.findById(c.id()).orElseThrow().status);
    }
}
