package com.mlb.mlbportal.controllers;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlb.mlbportal.dto.authentication.ForgotPasswordRequest;
import com.mlb.mlbportal.dto.authentication.RegisterRequest;
import com.mlb.mlbportal.dto.authentication.ResetPasswordRequest;
import com.mlb.mlbportal.dto.user.UserRole;
import com.mlb.mlbportal.security.jwt.AuthResponse;
import com.mlb.mlbportal.security.jwt.AuthResponse.Status;
import com.mlb.mlbportal.security.jwt.LoginRequest;
import com.mlb.mlbportal.security.jwt.UserLoginService;
import com.mlb.mlbportal.services.EmailService;
import com.mlb.mlbportal.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints related to user authentication")
public class AuthController {

    private final UserLoginService userLoginService;
    private final UserService userService;
    private final EmailService emailService;

    public AuthController(UserLoginService userLoginService, UserService userService, EmailService emailService) {
        this.userLoginService = userLoginService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @Operation(summary = "Get the active user", description = "Obtain details of the currently authenticated user.", responses = {
            @ApiResponse(responseCode = "200", description = "User successfully authenticated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserRole.class), examples = @ExampleObject(value = "{\"username\":\"johndoe\",\"roles\":[\"USER\"]}"))),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @GetMapping("/me")
    public ResponseEntity<UserRole> getActiveUser(Principal principal) {
        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException("User Not Authenticated");
        }
        return ResponseEntity.ok(this.userService.getUserRole(principal.getName()));
    }

    @Operation(summary = "User login", description = "Authenticates the user and returns JWT tokens (access and refresh).", responses = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"Auth successful. Tokens are created in cookie.\"}"))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        return this.userLoginService.login(response, loginRequest);
    }

    @Operation(summary = "Register new user", description = "Creates a new user account in the system.", responses = {
            @ApiResponse(responseCode = "200", description = "User successfully registered", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"User registered successfully\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid registration data"),
            @ApiResponse(responseCode = "409", description = "User already exists in the database")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        this.userService.createUser(registerRequest);
        return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, "User registered successfully"));
    }

    @Operation(summary = "Refresh JWT token", description = "Generates new JWT tokens using the refresh token stored in cookies.", responses = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"Tokens refreshed\"}"))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Parameter(description = "Refresh token (from cookie named RefreshToken)") @CookieValue(name = "RefreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        return userLoginService.refresh(response, refreshToken);
    }

    @Operation(summary = "User logout", description = "Clears authentication cookies and invalidates session tokens.", responses = {
            @ApiResponse(responseCode = "200", description = "Logout successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"logout successfully\"}")))
    })
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logOut(HttpServletResponse response) {
        return this.userLoginService.logout(response);
    }

    @Operation(summary = "Request password recovery", description = "Sends a recovery email to the user containing a reset link or verification code.", responses = {
            @ApiResponse(responseCode = "200", description = "Recovery email sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"Recovery email sent successfully\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid email address"),
            @ApiResponse(responseCode = "404", description = "There is no user registered with this email")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        this.emailService.sendEmail(request.email());
        return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, "Recovery email sent successfully"));
    }

    @Operation(summary = "Reset user password", description = "Allows the user to set a new password using a valid recovery code.", responses = {
            @ApiResponse(responseCode = "200", description = "Password successfully reset", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"Password restored\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired recovery code")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        boolean success = this.userService.resetPassword(request.code(), request.newPassword());
        if (success) {
            return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, "Password restored"));
        }
        return ResponseEntity.badRequest().body(new AuthResponse(Status.FAILURE, "Invalid or expired code"));
    }
}