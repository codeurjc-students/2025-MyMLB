package com.mlb.mlbportal.handler.conflict;

public class PlayerAlreadyExistsException extends ResourceAlreadyExistsException {
    public PlayerAlreadyExistsException() {
        super("Player Already Exists");
    }
}