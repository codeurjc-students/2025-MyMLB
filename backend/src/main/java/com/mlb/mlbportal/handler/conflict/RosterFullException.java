package com.mlb.mlbportal.handler.conflict;

public class RosterFullException extends ResourceAlreadyExistsException {
    public RosterFullException(String message) {
        super(message);
    }
}
