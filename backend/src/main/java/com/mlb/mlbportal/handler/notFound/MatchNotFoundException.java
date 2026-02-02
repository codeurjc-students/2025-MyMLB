package com.mlb.mlbportal.handler.notFound;

public class MatchNotFoundException extends ResourceNotFoundException {
    public MatchNotFoundException() {
        super("Match Not Found");
    }
}