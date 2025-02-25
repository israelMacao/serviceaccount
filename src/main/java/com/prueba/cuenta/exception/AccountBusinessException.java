package com.prueba.cuenta.exception;

public class AccountBusinessException extends RuntimeException {
    public AccountBusinessException(String message) {
        super(message);
    }
}
