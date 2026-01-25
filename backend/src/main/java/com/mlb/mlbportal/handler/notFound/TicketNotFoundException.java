package com.mlb.mlbportal.handler.notFound;

public class TicketNotFoundException extends ResourceNotFoundException {
    public TicketNotFoundException(Long ticketId) {
        super("Ticket " + ticketId + " Not Found");
    }
}