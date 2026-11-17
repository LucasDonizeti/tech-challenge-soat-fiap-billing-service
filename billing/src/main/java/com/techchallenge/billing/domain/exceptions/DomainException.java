package com.techchallenge.billing.domain.exceptions;

public abstract class DomainException extends RuntimeException {

    private final String errorCode;

    protected DomainException(String message) {
        this(message, null);
    }

    protected DomainException(String message, String errorCode) {
        this(message, errorCode, null);
    }

    protected DomainException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : this.getClass().getSimpleName();
    }

    public String getErrorCode() {
        return errorCode;
    }
}
