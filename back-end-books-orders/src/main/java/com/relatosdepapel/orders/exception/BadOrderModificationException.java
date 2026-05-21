package com.relatosdepapel.orders.exception;

public class BadOrderModificationException extends RuntimeException {

    public BadOrderModificationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
