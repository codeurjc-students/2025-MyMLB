package com.mlb.mlbportal.handler.badRequest;

public class MaxiumPicturesExceededException extends RuntimeException {
    public MaxiumPicturesExceededException() {
        super("A maximum of 5 images is required");
    }
}