package com.mlb.mlbportal.handler.alreadyExists;

public abstract class ResourceAlreadyExistsException extends RuntimeException {
    protected ResourceAlreadyExistsException(String message) {
        super(message);
    }
}