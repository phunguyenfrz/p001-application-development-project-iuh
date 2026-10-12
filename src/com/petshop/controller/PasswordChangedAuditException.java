package com.petshop.controller;

public final class PasswordChangedAuditException extends IllegalStateException {
    public PasswordChangedAuditException(String message, Throwable cause) {
        super(message, cause);
    }
}
