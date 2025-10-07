package com.mlb.mlbportal.dto.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @Email(message = "Invalid email format")
        @NotBlank(message = "The email is required")
        String email,

        @NotBlank(message = "The username is required")
        String username,

        @NotBlank(message = "The password is required")
        String password
) {}