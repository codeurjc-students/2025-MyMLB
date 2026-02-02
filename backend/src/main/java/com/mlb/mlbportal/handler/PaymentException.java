package com.mlb.mlbportal.handler;

public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
}