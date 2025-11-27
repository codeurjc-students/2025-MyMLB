package com.mlb.mlbportal.handler.badRequest;

public class MaxiumPicturesExceededException extends RuntimeException {
  public MaxiumPicturesExceededException(String message) {
    super(message);
  }
}
