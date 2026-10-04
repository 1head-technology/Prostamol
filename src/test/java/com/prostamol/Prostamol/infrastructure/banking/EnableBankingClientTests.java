package com.prostamol.Prostamol.infrastructure.banking;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

class EnableBankingClientTests {
    @org.junit.jupiter.api.io.TempDir java.nio.file.Path temp;

    @Test void signsExpectedApplicationJwtWithRegisteredRsaKey() throws Exception {
        var generator = java.security.KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var pair = generator.generateKeyPair();
        var path = temp.resolve("test.pem");
        java.nio.file.Files.writeString(path, "-----BEGIN PRIVATE KEY-----\n"
            + java.util.Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()) + "\n-----END PRIVATE KEY-----");
        String id = java.util.UUID.randomUUID().toString();
        var client = new EnableBankingClient(id, path.toString(), "http://localhost:3000/banking/callback");
        var signed = io.jsonwebtoken.Jwts.parser().verifyWith(pair.getPublic()).build().parseSignedClaims(client.token());
        assertEquals("RS256", signed.getHeader().getAlgorithm());
        assertEquals(id, signed.getHeader().getKeyId());
        assertEquals("JWT", signed.getHeader().getType());
        assertEquals("enablebanking.com", signed.getPayload().getIssuer());
        assertTrue(signed.getPayload().getAudience().contains("api.enablebanking.com"));
        assertEquals(3600000, signed.getPayload().getExpiration().getTime() - signed.getPayload().getIssuedAt().getTime());
        var raw = new ObjectMapper().readTree(java.util.Base64.getUrlDecoder().decode(client.token().split("\\.")[1]));
        assertTrue(raw.path("aud").isString());
    }
    @Test void mapsBookedEntriesAndSkipsUnstableIdentifiersAndPending() {
        var page = EnableBankingClient.parsePage(new ObjectMapper().readTree("""
            {"transactions":[
              {"entry_reference":"stable","status":"BOOK","credit_debit_indicator":"CRDT",
               "transaction_amount":{"amount":"23.50","currency":"EUR"},"value_date":"2026-09-01",
               "remittance_information":["Salary","September"]},
              {"transaction_id":"unstable","status":"BOOK","booking_date":"2026-09-01"},
              {"entry_reference":"pending","status":"PDNG"}
            ],"continuation_key":"next+/="}
            """));
        assertEquals(1, page.entries().size());
        assertTrue(page.entries().getFirst().credit());
        assertEquals("Salary September", page.entries().getFirst().description());
        assertEquals(1, page.skipped());
        assertEquals("next+/=", page.continuationKey());
    }
    @Test void missingListDoesNotSilentlyCountAsSuccessfulSync() {
        assertThrows(BankingException.class, () -> EnableBankingClient.parsePage(new ObjectMapper().readTree("{}")));
    }
    @Test void providerErrorsDistinguishEndedConsentFromOtherUnauthorizedResponses() {
        var expired = EnableBankingException.fromResponse(401,
            "{\"error\":\"EXPIRED_SESSION\",\"message\":\"private detail\"}");
        assertEquals(502, expired.status());
        assertEquals("EXPIRED", expired.connectionStatus());
        assertTrue(expired.getMessage().contains("EXPIRED_SESSION"));
        assertTrue(expired.getMessage().contains("reconnect"));
        assertFalse(expired.getMessage().contains("private detail"));
        for (String code : java.util.List.of("REVOKED_SESSION", "CLOSED_SESSION", "SESSION_DOES_NOT_EXIST")) {
            assertEquals("FAILED", EnableBankingException.fromResponse(401,
                "{\"error\":\"" + code + "\"}").connectionStatus());
        }
        var unauthorized = EnableBankingException.fromResponse(401, "{\"error\":\"UNAUTHORIZED_ACCESS\"}");
        assertNull(unauthorized.connectionStatus());
        assertTrue(unauthorized.getMessage().contains("UNAUTHORIZED_ACCESS"));
    }

    @Test void malformedAndUnknownErrorsDoNotExposeResponseBodies() {
        for (String body : java.util.List.of("<html>private detail</html>", "null", "{}",
            "{\"error\":\"private detail\",\"message\":\"secret\"}")) {
            var error = EnableBankingException.fromResponse(401, body);
            assertEquals("Enable Banking request failed (HTTP 401)", error.getMessage());
            assertNull(error.connectionStatus());
        }
    }

    @Test void bookedBalancePrefersInterimThenClosingInAccountCurrency() {
        var response = new ObjectMapper().readTree("""
            {"balances":[
                {"balance_type":"ITBD","balance_amount":{"amount":"99","currency":"USD"}},
                {"balance_type":"CLBD","balance_amount":{"amount":"12","currency":"EUR"}},
                {"balance_type":"ITBD","balance_amount":{"amount":"-23.50","currency":"EUR"}}
            ]}
            """);
        assertEquals(new java.math.BigDecimal("-23.50"), EnableBankingClient.parseBalance(response, "EUR").booked().amount());
        var closing = new ObjectMapper().readTree("""
            {"balances":[{"balance_type":"CLBD","balance_amount":{"amount":"0","currency":"EUR"}}]}
            """);
        assertEquals(0, EnableBankingClient.parseBalance(closing, "EUR").booked().amount().signum());
        assertNull(EnableBankingClient.parseBalance(closing, "GBP"));
    }

    @Test void availableOnlyBalancesAreKeptSeparateAndEmptyBalancesRemainUnavailable() {
        var available = new ObjectMapper().readTree("""
            {"balances":[{"balance_type":"ITAV","balance_amount":{"amount":"100","currency":"EUR"}}]}
            """);
        var parsed = EnableBankingClient.parseBalance(available, "EUR");
        assertNull(parsed.booked());
        assertEquals(new java.math.BigDecimal("100"), parsed.available().amount());
        assertNull(EnableBankingClient.parseBalance(new ObjectMapper().readTree("{\"balances\":[]}"), "EUR"));
        assertThrows(BankingException.class,
            () -> EnableBankingClient.parseBalance(new ObjectMapper().readTree("{}"), "EUR"));
    }

    @Test void availableBalancePrefersInterimAndFiltersCurrencyWithoutReplacingBooked() {
        var response = new ObjectMapper().readTree("""
            {"balances":[
                {"balance_type":"ITAV","balance_amount":{"amount":"999","currency":"USD"}},
                {"balance_type":"CLAV","balance_amount":{"amount":"12","currency":"EUR"}},
                {"balance_type":"ITAV","balance_amount":{"amount":"0","currency":"EUR"}},
                {"balance_type":"CLBD","balance_amount":{"amount":"20","currency":"EUR"}}
            ]}
            """);
        var balances = EnableBankingClient.parseBalance(response, "EUR");
        assertEquals(new java.math.BigDecimal("20"), balances.booked().amount());
        assertEquals(0, balances.available().amount().signum());
        var closing = EnableBankingClient.parseBalance(new ObjectMapper().readTree("""
            {"balances":[{"balance_type":"CLAV","balance_amount":{"amount":"-3.50","currency":"EUR"}}]}
            """), "EUR");
        assertNull(closing.booked());
        assertEquals(new java.math.BigDecimal("-3.50"), closing.available().amount());
    }

    @Test void unsupportedBalanceDiagnosticsExplainTypesWithoutExposingPrivateData() {
        var logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(EnableBankingClient.class);
        var appender = new ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        try {
            var response = new ObjectMapper().readTree("""
                {"account_id":"private-account", "balances":[
                    {"balance_type":"ITAV","balance_amount":{"amount":"1234.56","currency":"EUR"}},
                    {"balance_type":"CLBD","balance_amount":{"amount":"9876.54","currency":"USD"}},
                    {"balance_type":"private-secret\\nforged-log","balance_amount":{"currency":"private-currency"}},
                    {}
                ]}
                """);
            assertNull(EnableBankingClient.parseBalance(response, "GBP"));
            assertEquals(1, appender.list.size());
            var event = appender.list.getFirst();
            assertEquals(ch.qos.logback.classic.Level.WARN, event.getLevel());
            assertEquals("Enable Banking: no supported balance for currency=GBP; balanceCount=4; "
                + "offeredTypesAndCurrencies=[ITAV/EUR, CLBD/USD, invalid/invalid, missing/missing] (up to 20); "
                + "supportedTypes=[ITBD, CLBD, ITAV, CLAV]", event.getFormattedMessage());

            appender.list.clear();
            assertNotNull(EnableBankingClient.parseBalance(response, "USD"));
            assertTrue(appender.list.isEmpty(), "Supported booked balances should not emit a warning");

            EnableBankingClient.parseBalance(new ObjectMapper().readTree("{\"balances\":[]}"), "EUR");
            assertTrue(appender.list.getFirst().getFormattedMessage().contains("balanceCount=0"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
