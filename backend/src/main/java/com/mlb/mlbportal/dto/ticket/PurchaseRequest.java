package com.mlb.mlbportal.dto.ticket;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PurchaseRequest(
        Long eventManagerId,

        @NotNull(message = "the amount is required")
        Integer ticketAmount,

        @NotNull(message = "The cardnumber is required")
        String cardNumber,

        @NotNull(message = "The cvv is required")
        String cvv,

        @NotNull(message = "The expiration date is required")
        LocalDate expirationDate
) {}