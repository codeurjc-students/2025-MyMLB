package com.mlb.mlbportal.dto.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest (
    @NotBlank(message = "Code can not be empty")
    @Pattern(regexp = "\\d{4}", message = "Code must be a 4 digits number")
    String code,

    @NotBlank(message = "The new password can not be empty")
    String newPassword
) {}