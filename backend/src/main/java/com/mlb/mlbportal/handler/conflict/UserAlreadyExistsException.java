package com.mlb.mlbportal.handler.conflict;

public class UserAlreadyExistsException extends ResourceAlreadyExistsException {
    public UserAlreadyExistsException() {
        super("The User Already Exists in the Database");
    }   
}