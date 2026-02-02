package com.mlb.mlbportal.dto.ticket;

import java.time.YearMonth;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PurchaseRequest(
        Long eventManagerId,

        @Min(1)
        @NotNull(message = "the amount is required")
        Integer ticketAmount,

        @Size(min = 1)
        @NotNull(message = "The seats are required")
        List<SeatDTO> seats,

        @NotBlank(message = "The owner's name cannot be empty")
        @NotNull(message = "The owner's name is required")
        String ownerName,

        @NotBlank(message = "The cardnumber cannot be empty")
        @NotNull(message = "The cardnumber is required")
        String cardNumber,

        @NotBlank(message = "The cvv cannot be empty")
        @NotNull(message = "The cvv is required")
        String cvv,

        @JsonFormat(pattern = "MM/yy")
        @NotNull(message = "The expiration date is required")
        YearMonth expirationDate
) {}