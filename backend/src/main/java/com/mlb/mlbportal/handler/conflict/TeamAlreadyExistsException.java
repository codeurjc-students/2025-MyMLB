package com.mlb.mlbportal.handler.alreadyExists;

public class TeamAlreadyExistsException extends ResourceAlreadyExistsException {
    public TeamAlreadyExistsException() {
        super("Team Already Exists");
    }
}