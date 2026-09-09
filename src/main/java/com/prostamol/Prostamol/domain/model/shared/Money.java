package com.prostamol.Prostamol.domain.model.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, String currency) {

    public static final int MAX_PRECISION = 19;
    public static final int MAX_SCALE = 4;

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");

        if (!currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("currency must be an uppercase ISO-4217 code");
        }
        try {
            Currency.getInstance(currency);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported ISO-4217 currency: " + currency, ex);
        }

        try {
            BigDecimal persisted = amount.setScale(MAX_SCALE, RoundingMode.UNNECESSARY);
            if (persisted.precision() > MAX_PRECISION) {
                throw new IllegalArgumentException(
                    "amount exceeds precision " + MAX_PRECISION + " and scale " + MAX_SCALE);
            }
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("amount must have at most " + MAX_SCALE + " decimal places", ex);
        }
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money requirePositive(String fieldName) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
        return this;
    }

    public Money requireNonNegative(String fieldName) {
        if (amount.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return this;
    }

    public void assertSameCurrency(Money other) {
        Objects.requireNonNull(other, "other money must not be null");
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + this.currency + " vs " + other.currency);
        }
    }
}
