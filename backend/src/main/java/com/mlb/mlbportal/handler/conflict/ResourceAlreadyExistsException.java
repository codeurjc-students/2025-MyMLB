package com.mlb.mlbportal.handler.conflict;

public abstract class ResourceAlreadyExistsException extends RuntimeException {
    protected ResourceAlreadyExistsException(String message) {
        super(message);
    }
}