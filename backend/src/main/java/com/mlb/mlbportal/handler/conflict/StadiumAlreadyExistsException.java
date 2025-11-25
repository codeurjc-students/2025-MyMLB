package com.mlb.mlbportal.handler.conflict;

public class StadiumAlreadyExistsException extends ResourceAlreadyExistsException {
    public StadiumAlreadyExistsException() {
        super("Stadium Already Exists");
    }
}