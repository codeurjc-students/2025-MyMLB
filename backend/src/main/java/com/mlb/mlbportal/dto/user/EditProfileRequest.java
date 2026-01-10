package com.mlb.mlbportal.dto.user;

import jakarta.validation.constraints.Email;
import org.springframework.lang.Nullable;

import java.util.Optional;

public record EditProfileRequest(
        @Nullable
        @Email(message = "The email must be in a valid format")
        String email,

        @Nullable
        String password
) {}