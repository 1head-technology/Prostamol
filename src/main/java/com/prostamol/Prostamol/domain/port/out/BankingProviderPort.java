package com.prostamol.Prostamol.domain.port.out;

import com.prostamol.Prostamol.domain.model.shared.Money;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface BankingProviderPort {
    record Bank(String name, String country, long maximumConsentValidity) {}
    record Authorization(String url) {}
    record RemoteAccount(String uid, String identificationHash, String name, String currency) {}
    record Session(String id, Instant validUntil, List<RemoteAccount> accounts) {}
    record Entry(String reference, Money amount, boolean credit, LocalDate date, String description) {}
    record Page(List<Entry> entries, String continuationKey, int skipped) {}
    List<Bank> banks(String country);
    Authorization authorize(Bank bank, String psuType, String state, String userId, Instant validUntil);
    Session exchange(String code);
    Page transactions(String accountUid, LocalDate from, LocalDate to, String continuationKey);
    Money balance(String accountUid, String currency);
    void disconnect(String sessionId);
}
