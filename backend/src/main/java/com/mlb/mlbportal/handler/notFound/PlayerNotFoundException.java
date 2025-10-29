package com.mlb.mlbportal.handler.notFound;

public class PlayerNotFoundException extends ResourceNotFoundException {
    public PlayerNotFoundException() {
        super("Player Not Found");
    }   
}