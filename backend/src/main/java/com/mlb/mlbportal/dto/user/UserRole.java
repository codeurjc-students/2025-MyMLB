package com.mlb.mlbportal.dto.user;

import java.util.List;

public record UserRole(
    String username,

    List<String> roles
){}