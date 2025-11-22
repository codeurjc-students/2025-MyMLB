package com.mlb.mlbportal.handler;

public class InvalidSearchTypeException extends RuntimeException {
    public InvalidSearchTypeException(String message) {
        super(message);
    }   
}