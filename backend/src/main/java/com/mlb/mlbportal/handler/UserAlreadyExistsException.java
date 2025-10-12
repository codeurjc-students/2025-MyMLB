package com.mlb.mlbportal.handler;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super("The User Already Exists in the Database");
    }   
}