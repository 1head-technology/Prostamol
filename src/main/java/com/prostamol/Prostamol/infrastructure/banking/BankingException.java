package com.prostamol.Prostamol.infrastructure.banking;

public class BankingException extends RuntimeException {
    private final int status;
    public BankingException(int status, String message) { super(message); this.status = status; }
    public int status() { return status; }
}
