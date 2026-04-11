package com.mlb.mlbportal.handler.conflict;

public class RankingRegisterAlreadyExistsException extends RuntimeException {
    public RankingRegisterAlreadyExistsException(String message) {
        super(message);
    }
}