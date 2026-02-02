package com.mlb.mlbportal.handler.conflict;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException() {
        super("Inssuficient Stock");
    }
}