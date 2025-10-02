package com.mlb.mlbportal.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlb.mlbportal.dto.authentication.RegisterRequest;
import com.mlb.mlbportal.security.jwt.AuthResponse;
import com.mlb.mlbportal.security.jwt.AuthResponse.Status;
import com.mlb.mlbportal.security.jwt.LoginRequest;
import com.mlb.mlbportal.security.jwt.UserLoginService;
import com.mlb.mlbportal.services.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserLoginService userLoginService;
    private final UserService userService;

    public AuthController(UserLoginService userLoginService, UserService userService) {
        this.userLoginService = userLoginService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return this.userLoginService.login(response, loginRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        this.userService.createUser(registerRequest);
        return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, "User registered successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue(name = "RefreshToken", required = false) String refreshToken, HttpServletResponse response) {
        return userLoginService.refresh(response, refreshToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logOut(HttpServletResponse response) {
        return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, this.userLoginService.logout(response)));
    }
}