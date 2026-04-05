package com.mlb.mlbportal.handler.badRequest;

public class InvalidTypeException extends RuntimeException {
    public InvalidTypeException(String message) {
        super(message);
    }
}