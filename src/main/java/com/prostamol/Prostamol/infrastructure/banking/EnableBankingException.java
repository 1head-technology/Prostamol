package com.prostamol.Prostamol.infrastructure.banking;

import java.util.Set;
import tools.jackson.databind.ObjectMapper;

final class EnableBankingException extends BankingException {
    private static final Set<String> KNOWN_ERRORS = Set.of(
        "ACCESS_DENIED", "ACCOUNT_DOES_NOT_EXIST", "ASPSP_ACCOUNT_NOT_ACCESSIBLE",
        "ASPSP_ERROR", "ASPSP_PSU_ACTION_REQUIRED", "ASPSP_RATE_LIMIT_EXCEEDED",
        "ASPSP_TIMEOUT", "AUTHORIZATION_NOT_PROVIDED", "CLOSED_SESSION",
        "EXPIRED_AUTHORIZATION_CODE", "EXPIRED_SESSION", "INVALID_ACCOUNT_ID",
        "INVALID_HOST", "NO_ACCOUNTS_ADDED", "PSU_HEADER_INVALID", "PSU_HEADER_NOT_PROVIDED",
        "REDIRECT_URI_NOT_ALLOWED", "REVOKED_SESSION", "SESSION_DOES_NOT_EXIST",
        "UNAUTHORIZED_ACCESS", "UNAUTHORIZED_IP", "WRONG_ASPSP_PROVIDED",
        "WRONG_AUTHORIZATION_CODE", "WRONG_CONTINUATION_KEY", "WRONG_CREDENTIALS_PROVIDED",
        "WRONG_DATE_INTERVAL", "WRONG_REQUEST_PARAMETERS", "WRONG_SESSION_STATUS",
        "WRONG_TRANSACTIONS_PERIOD", "DATE_FROM_IN_FUTURE", "DATE_TO_WITHOUT_DATE_FROM"
    );
    private static final Set<String> ENDED_SESSIONS = Set.of(
        "EXPIRED_SESSION", "CLOSED_SESSION", "REVOKED_SESSION", "SESSION_DOES_NOT_EXIST"
    );
    private final String providerError;

    private EnableBankingException(int httpStatus, String providerError) {
        super(502, "Enable Banking request failed (HTTP " + httpStatus
            + (providerError == null ? "" : ", " + providerError) + ")"
            + (providerError != null && ENDED_SESSIONS.contains(providerError)
                ? "; bank access ended; reconnect the bank" : ""));
        this.providerError = providerError;
    }

    static EnableBankingException fromResponse(int httpStatus, String body) {
        String error = null;
        try {
            var value = new ObjectMapper().readTree(body).path("error");
            if (value.isString() && KNOWN_ERRORS.contains(value.asText())) {
                error = value.asText();
            }
        } catch (RuntimeException ignored) {
            // An upstream HTML or malformed response must retain its HTTP status.
        }
        return new EnableBankingException(httpStatus, error);
    }

    boolean sessionEnded() {
        return providerError != null && ENDED_SESSIONS.contains(providerError);
    }

    String connectionStatus() {
        if ("EXPIRED_SESSION".equals(providerError)) {
            return "EXPIRED";
        }
        if (sessionEnded()) {
            return "FAILED";
        }
        return null;
    }
}
