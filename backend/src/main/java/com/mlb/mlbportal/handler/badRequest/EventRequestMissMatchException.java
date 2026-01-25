package com.mlb.mlbportal.handler.badRequest;

public class EventRequestMissMatchException extends RuntimeException {
    public EventRequestMissMatchException(String message) {
        super(message);
    }
}