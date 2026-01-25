package com.mlb.mlbportal.handler;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.ServiceUnavailableException;

import com.mlb.mlbportal.handler.badRequest.EventRequestMissMatchException;
import com.mlb.mlbportal.handler.badRequest.SeatSelectionMismatchException;
import com.mlb.mlbportal.handler.conflict.*;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mlb.mlbportal.handler.badRequest.MaxiumPicturesExceededException;
import com.mlb.mlbportal.handler.notFound.ResourceNotFoundException;

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

    @ExceptionHandler(MaxiumPicturesExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMaximumPicturesExceeded(MaxiumPicturesExceededException ex) {
        return this.buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "A maximum of 5 images is required");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException ex) {
        return this.buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "");
    }

    @ExceptionHandler(SeatSelectionMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleSeatSelectionMissMatch(SeatSelectionMismatchException ex) {
        return this.buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "Seat Selection MissMatch");
    }

    @ExceptionHandler(EventRequestMissMatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleEditEventRequestMissMatch(EventRequestMissMatchException ex) {
        return this.buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "Request MissMatch");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleResourceNotFound(ResourceNotFoundException ex) {
        String message = (ex.getMessage() == null) ? "Resource Not Found" : ex.getMessage();
        return buildResponse(HttpStatus.NOT_FOUND, message, "Resource Not Found");
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        String message = (ex.getMessage() == null) ? "Resource Already Exists" : ex.getMessage();
        return buildResponse(HttpStatus.CONFLICT, message, "Resource Already Exists");
    }

    @ExceptionHandler(LastPictureDeletionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleLastPictureDeletion(LastPictureDeletionException ex) {
        return this.buildResponse(HttpStatus.CONFLICT, ex.getMessage(), "Cannot delete the last picture of a stadium");
    }

    @ExceptionHandler(OptimisticEntityLockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleOptimisticLock(OptimisticEntityLockException ex) {
        String message = "This ticket has been answered by another admin. Please, refresh your inbox.";
        return this.buildResponse(HttpStatus.CONFLICT, message, "Concurrent Update Conflict");
    }

    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleInssuficientStock(InsufficientStockException ex) {
        return this.buildResponse(HttpStatus.CONFLICT, ex.getMessage(), "Cannot purchase the amount of tickets selected because there are no tickets available to purchase");
    }

    @ExceptionHandler(ConcurrentModificationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleConcurrentModification(ConcurrentModificationException ex) {
        return this.buildResponse(HttpStatus.CONFLICT, ex.getMessage(), "The transaction could not be completed due to a simultaneous update. Please refresh the page and try again");
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

    @ExceptionHandler(ServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, Object> handleServiceUnavailable(ServiceUnavailableException ex) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), "Service Unavailable");
    }

    @ExceptionHandler(PaymentException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, Object> handlePayment(PaymentException ex) {
        return this.buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), "Payment Business Error");
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
}