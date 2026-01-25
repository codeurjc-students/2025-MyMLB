package com.mlb.mlbportal.handler.badRequest;

public class SeatSelectionMismatchException extends RuntimeException {
    public SeatSelectionMismatchException() {
        super("The amount of selected seats does not match with the total of tickets selected");
    }
}
