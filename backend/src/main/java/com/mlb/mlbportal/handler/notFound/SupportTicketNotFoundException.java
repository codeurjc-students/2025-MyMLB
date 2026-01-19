package com.mlb.mlbportal.handler.notFound;

public class SupportTicketNotFoundException extends ResourceNotFoundException {
    public SupportTicketNotFoundException() {
        super("Ticket Not Found");
    }
}