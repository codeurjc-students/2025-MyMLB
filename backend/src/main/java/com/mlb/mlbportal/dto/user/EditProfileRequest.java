package com.mlb.mlbportal.dto.user;

import org.springframework.lang.Nullable;

import jakarta.validation.constraints.Email;

public record EditProfileRequest(
        @Nullable
        @Email(message = "The email must be in a valid format")
        String email,

        @Nullable
        String password
) {}