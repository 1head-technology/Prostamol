package com.prostamol.Prostamol.infrastructure.banking;

import com.prostamol.Prostamol.domain.port.out.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "enable-banking.enabled", havingValue = "true")
public class BankingConfig {
    @Bean
    BankingProviderPort bankingProvider(@Value("${enable-banking.application-id}") String id,
        @Value("${enable-banking.private-key-path}") String key,
        @Value("${enable-banking.redirect-url}") String redirect) {
        return new EnableBankingClient(id, key, redirect);
    }
    @Bean
    BankingService bankingService(BankingProviderPort provider, BankConnectionRepository connections,
        BankAccountLinkRepository links, ImportedBankEntryRepository receipts, AccountRepositoryPort accounts,
        TransactionRepositoryPort transactions, PlatformTransactionManager manager,
        @Value("${enable-banking.history-days:90}") int historyDays, jakarta.persistence.EntityManager entityManager) {
        if (historyDays < 1 || historyDays > 730) throw new IllegalArgumentException("Bank history days must be 1–730");
        TransactionTemplate tx = new TransactionTemplate(manager);
        tx.setTimeout(300);
        return new BankingService(provider, connections, links, receipts, accounts, transactions,
            tx, historyDays, entityManager);
    }
    @Bean
    @ConditionalOnProperty(name = "enable-banking.sync-enabled", havingValue = "true", matchIfMissing = true)
    BankingScheduler bankingScheduler(BankingService service) { return new BankingScheduler(service); }

    static class BankingScheduler {
        private final BankingService service;
        BankingScheduler(BankingService service) { this.service = service; }
        @Scheduled(fixedDelayString = "${enable-banking.sync-delay-ms:21600000}", initialDelayString = "${enable-banking.sync-delay-ms:21600000}")
        public void sync() { service.syncDue(); }
    }
}
