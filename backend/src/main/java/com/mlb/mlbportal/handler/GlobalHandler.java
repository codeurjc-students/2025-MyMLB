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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        errors.put("statusCode", HttpStatus.BAD_REQUEST.value());
        errors.put("status", "FAILURE");
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("statusCode", HttpStatus.CONFLICT.value());
        body.put("status", "FAILURE");
        body.put("message", ex.getMessage());
        body.put("error", "User Already Exists in the Database");
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("statusCode", HttpStatus.NOT_FOUND.value());
        body.put("status", "FAILURE");
        body.put("message", ex.getMessage());
        body.put("error", "User Not Found");
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}