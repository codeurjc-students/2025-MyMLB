package com.mlb.mlbportal.handler.conflict;

public class SupportTicketAlreadyIsClosedException extends ResourceAlreadyExistsException {
    public SupportTicketAlreadyIsClosedException() {
        super("Support Ticket already been closed");
    }
}