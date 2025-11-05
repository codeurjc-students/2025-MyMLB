package com.mlb.mlbportal.handler.notFound;

public class StadiumNotFoundException extends ResourceNotFoundException {

    public StadiumNotFoundException() {
        super("Stadium Not Found");   
    }
}