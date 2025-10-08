package com.mlb.mlbportal.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalHandler {
    private static final String FAILURE = "FAILURE";
    private static final String STATUS_CODE = "statusCode";
    private static final String STATUS = "status";
    private static final String MESSAGE = "message";
    private static final String ERROR = "error";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        errors.put(STATUS_CODE, HttpStatus.BAD_REQUEST.value());
        errors.put(STATUS, FAILURE);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put(STATUS_CODE, HttpStatus.CONFLICT.value());
        body.put(STATUS, FAILURE);
        body.put(MESSAGE, ex.getMessage());
        body.put(ERROR, "User Already Exists in the Database");
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put(STATUS_CODE, HttpStatus.NOT_FOUND.value());
        body.put(STATUS, FAILURE);
        body.put(MESSAGE, ex.getMessage());
        body.put(ERROR, "User Not Found");
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}