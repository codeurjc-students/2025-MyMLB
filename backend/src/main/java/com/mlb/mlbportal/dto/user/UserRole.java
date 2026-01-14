package com.mlb.mlbportal.dto.user;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record UserRole(
    @NotBlank(message= "Username is required")
    String username,

    List<String> roles,

    String email,

    String password
){}