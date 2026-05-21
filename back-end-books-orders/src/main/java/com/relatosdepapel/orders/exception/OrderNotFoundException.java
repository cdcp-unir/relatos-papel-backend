package com.relatosdepapel.orders.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
