package com.mlb.mlbportal.handler;

import com.mlb.mlbportal.handler.alreadyExists.ResourceAlreadyExistsException;

public class UserAlreadyExistsException extends ResourceAlreadyExistsException {
    public UserAlreadyExistsException() {
        super("The User Already Exists in the Database");
    }   
}