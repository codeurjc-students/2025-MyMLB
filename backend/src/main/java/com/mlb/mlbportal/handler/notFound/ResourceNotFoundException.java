package com.mlb.mlbportal.handler.notFound;

import java.util.NoSuchElementException;

public abstract class ResourceNotFoundException extends NoSuchElementException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}