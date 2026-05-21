package com.relatosdepapel.orders.exception;

public class InternalServerException extends RuntimeException {
    public InternalServerException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
