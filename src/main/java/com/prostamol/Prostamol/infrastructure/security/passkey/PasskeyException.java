package com.prostamol.Prostamol.infrastructure.security.passkey;

public class PasskeyException extends RuntimeException {
    public PasskeyException() { super("Passkey verification failed. Please start again."); }
}
