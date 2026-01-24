package com.mlb.mlbportal.handler.notFound;

public class EventNotFoundException extends ResourceNotFoundException {
    public EventNotFoundException(Long id) {
        super("Event " + id + " Not Found");
    }

    public EventNotFoundException() {
        super("Event Not Found");
    }
}