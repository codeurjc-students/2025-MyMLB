package com.mlb.mlbportal.dto.support;

import jakarta.validation.constraints.NotBlank;

public record CreateTicketRequest(
        @NotBlank(message = "The email is required")
        String email,

        @NotBlank(message = "The subject is required")
        String subject,

        @NotBlank(message = "The body is required")
        String body
) {}