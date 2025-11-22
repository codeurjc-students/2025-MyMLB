package com.mlb.mlbportal.handler;

import java.util.HashMap;
import java.util.Map;

import javax.naming.ServiceUnavailableException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mlb.mlbportal.handler.conflict.LastPictureDeletionException;
import com.mlb.mlbportal.handler.conflict.ResourceAlreadyExistsException;
import com.mlb.mlbportal.handler.conflict.TeamAlreadyExistsException;
import com.mlb.mlbportal.handler.conflict.UserAlreadyExistsException;
import com.mlb.mlbportal.handler.notFound.PlayerNotFoundException;
import com.mlb.mlbportal.handler.notFound.ResourceNotFoundException;
import com.mlb.mlbportal.handler.notFound.StadiumNotFoundException;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.handler.notFound.UserNotFoundException;

@RestControllerAdvice
public class GlobalHandler {
    private static final String FAILURE = "FAILURE";

    private Map<String, Object> buildResponse(HttpStatus status, String message, String error) {
        Map<String, Object> body = new HashMap<>();
        body.put("statusCode", status.value());
        body.put("status", FAILURE);
        body.put("message", message);
        body.put("error", error);
        return body;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            body.put(error.getField(), error.getDefaultMessage())
        );
        body.put("statusCode", HttpStatus.BAD_REQUEST.value());
        body.put("status", FAILURE);
        return body;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleResourceNotFound(ResourceNotFoundException ex) {
        String errorMsg = "Resource Not Found";
        if (ex instanceof UserNotFoundException) errorMsg = "User Not Found";
        else if (ex instanceof TeamNotFoundException) errorMsg = "Team Not Found";
        else if (ex instanceof StadiumNotFoundException) errorMsg = "Stadium Not Found";
        else if (ex instanceof PlayerNotFoundException) errorMsg = "Player Not Found";

        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), errorMsg);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        String errorMsg = "Resource Already Exists";
        if (ex instanceof TeamAlreadyExistsException) {
            errorMsg = "Team Already Exists";
        }
        else if (ex instanceof UserAlreadyExistsException) {
            errorMsg = "User Already Exists";
        }
        String message = (ex.getMessage() == null) ? errorMsg : ex.getMessage();
        return buildResponse(HttpStatus.CONFLICT, message, errorMsg);
    }

    @ExceptionHandler(LastPictureDeletionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleLastPictureDeletion(LastPictureDeletionException ex) {
        return this.buildResponse(HttpStatus.CONFLICT, ex.getMessage(), "Cannot delete the last picture of a stadium");
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleNullPointer(NullPointerException ex) {
        if (ex.getMessage() == null || ex.getMessage().contains("getUser")) {
            return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), "User Not Authenticated");
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(),
                "Internal Server Error occurred due to NullPointerException");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Map<String, Object> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(),
                "HTTP method not allowed for this endpoint");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleAuthentication(AuthenticationException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), "Unauthorized");
    }

    @ExceptionHandler(InvalidSearchTypeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleInvalidSearchType(InvalidSearchTypeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "Invalid Search Type");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleAllOtherExceptions(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), "Internal Server Error");
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, Object> handleServiceUnavailable(ServiceUnavailableException ex) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), "Service Unavailable");
    }
}