package com.mlb.mlbportal.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalHandler {
    private final Map<String, Object> body = new HashMap<>();
    private static final String FAILURE = "FAILURE";
    private static final String STATUS_CODE = "statusCode";
    private static final String STATUS = "status";
    private static final String MESSAGE = "message";
    private static final String ERROR = "error";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        this.body.clear();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            this.body.put(error.getField(), error.getDefaultMessage())
        );
        this.body.put(STATUS_CODE, HttpStatus.BAD_REQUEST.value());
        this.body.put(STATUS, FAILURE);
        return new ResponseEntity<>(this.body, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        this.body.clear();
        this.body.put(STATUS_CODE, HttpStatus.CONFLICT.value());
        this.body.put(STATUS, FAILURE);
        this.body.put(MESSAGE, ex.getMessage());
        this.body.put(ERROR, "User Already Exists in the Database");
        return new ResponseEntity<>(this.body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        this.body.clear();
        this.body.put(STATUS_CODE, HttpStatus.NOT_FOUND.value());
        this.body.put(STATUS, FAILURE);
        this.body.put(MESSAGE, ex.getMessage());
        this.body.put(ERROR, "User Not Found");
        return new ResponseEntity<>(this.body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<?> handleNullPointer(NullPointerException ex) {
        this.body.clear();
        if (ex.getMessage() == null || ex.getMessage().contains("getUser")) {
            this.body.put(STATUS_CODE, HttpStatus.UNAUTHORIZED.value());
            this.body.put(STATUS, FAILURE);
            this.body.put(MESSAGE, ex.getMessage());
            this.body.put(ERROR, "User Not Authenticated");
            return new ResponseEntity<>(this.body, HttpStatus.UNAUTHORIZED);
        }
        this.body.put(STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
        this.body.put(STATUS, FAILURE);
        this.body.put(MESSAGE, ex.getMessage());
        this.body.put(ERROR, "Internal Server Error ocurred by a NullPointerException");
        return new ResponseEntity<>(this.body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMehtodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        this.body.clear();
        this.body.put(STATUS_CODE, HttpStatus.METHOD_NOT_ALLOWED.value());
        this.body.put(STATUS, FAILURE);
        this.body.put(MESSAGE, ex.getMessage());
        this.body.put(ERROR, "HTTP method not allowed for this endpoint");
        return new ResponseEntity<>(this.body, HttpStatus.METHOD_NOT_ALLOWED);
    }
}