package com.mlb.mlbportal.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.mlb.mlbportal.handler.notFound.PlayerNotFoundException;
import com.mlb.mlbportal.handler.notFound.ResourceNotFoundException;
import com.mlb.mlbportal.handler.notFound.StadiumNotFoundException;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.handler.notFound.UserNotFoundException;

@ControllerAdvice
public class GlobalHandler {
    private static final String FAILURE = "FAILURE";

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, String error) {
        Map<String, Object> body = new HashMap<>();
        body.put("statusCode", status.value());
        body.put("status", FAILURE);
        body.put("message", message);
        body.put("error", error);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            body.put(error.getField(), error.getDefaultMessage())
        );
        body.put("statusCode", HttpStatus.BAD_REQUEST.value());
        body.put("status", FAILURE);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @SuppressWarnings("null")
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        String errorMsg = "Resource Not Found";
        if (ex instanceof UserNotFoundException) {
            errorMsg = "User Not Found";
        }
        else if (ex instanceof TeamNotFoundException) {
            errorMsg = "Team Not Found";
        }
        else if (ex instanceof StadiumNotFoundException) {
            errorMsg = "Stadium Not Found";
        }
        else if (ex instanceof PlayerNotFoundException) {
            errorMsg = "Player Not Found";
        }
        return this.buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), errorMsg);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return this.buildResponse(HttpStatus.CONFLICT, ex.getMessage(), "User Already Exists in the Database");
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNullPointer(NullPointerException ex) {
        if (ex.getMessage() == null || ex.getMessage().contains("getUser")) {
            return this.buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), "User Not Authenticated");
        }
        return this.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(),
                "Internal Server Error occurred due to NullPointerException");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return this.buildResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(),
                "HTTP method not allowed for this endpoint");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        return this.buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(),"Unauthorized");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllOtherExceptions(Exception ex) {
        return this.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), "Internal Server Error");
    }
}