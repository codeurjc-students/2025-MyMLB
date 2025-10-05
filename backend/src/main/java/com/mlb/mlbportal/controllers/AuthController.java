package com.mlb.mlbportal.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlb.mlbportal.dto.authentication.ForgotPasswordRequest;
import com.mlb.mlbportal.dto.authentication.RegisterRequest;
import com.mlb.mlbportal.dto.authentication.ResetPasswordRequest;
import com.mlb.mlbportal.security.jwt.AuthResponse;
import com.mlb.mlbportal.security.jwt.AuthResponse.Status;
import com.mlb.mlbportal.security.jwt.LoginRequest;
import com.mlb.mlbportal.security.jwt.UserLoginService;
import com.mlb.mlbportal.services.EmailService;
import com.mlb.mlbportal.services.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserLoginService userLoginService;
    private final UserService userService;
    private final EmailService emailService;

    public AuthController(UserLoginService userLoginService, UserService userService, EmailService emailService) {
        this.userLoginService = userLoginService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
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

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        this.emailService.sendEmail(request.email());
        return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, "Recovery email successfully sended"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        boolean success = this.userService.resetPassword(request.code(), request.newPassword());
        if (success) {
            return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, "Password restored"));
        }
        return ResponseEntity.badRequest().body(new AuthResponse(Status.FAILURE, "Invalid or expired code"));
    } 
}