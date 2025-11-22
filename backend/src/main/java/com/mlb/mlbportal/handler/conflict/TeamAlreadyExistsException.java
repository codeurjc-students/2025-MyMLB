package com.mlb.mlbportal.handler.conflict;

public class TeamAlreadyExistsException extends ResourceAlreadyExistsException {
    public TeamAlreadyExistsException() {
        super("Team Already Exists");
    }
}