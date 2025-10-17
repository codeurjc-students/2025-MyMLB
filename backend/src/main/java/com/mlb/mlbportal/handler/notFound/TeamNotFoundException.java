package com.mlb.mlbportal.handler.notFound;

public class TeamNotFoundException extends ResourceNotFoundException {
    public TeamNotFoundException() {
        super("Team Not Found");
    }   
}