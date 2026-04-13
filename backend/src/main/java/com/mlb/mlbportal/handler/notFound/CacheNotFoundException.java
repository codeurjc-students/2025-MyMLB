package com.mlb.mlbportal.handler.notFound;

public class CacheNotFoundException extends ResourceNotFoundException {
    public CacheNotFoundException(String name) {
        super("Cache " + name + " Not Found");
    }
}