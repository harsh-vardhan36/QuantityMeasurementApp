package com.app.authservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.app.authservice.dto.*;
import com.app.authservice.security.JwtTokenProvider;
import com.app.authservice.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, login, password management, and token operations")
public class AuthController {

    private final AuthService       authService;
    private final JwtTokenProvider  tokenProvider;

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<String> register(@Valid @RequestBody SignUpRequest req) {
        authService.register(req);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/forgotPassword/{email}")
    @Operation(summary = "Request a password-reset OTP via email")
    public ResponseEntity<String> forgotPassword(@PathVariable String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok("OTP sent to " + email);
    }

    @PostMapping("/resetPassword/{email}")
    @Operation(summary = "Reset password using OTP received via email")
    public ResponseEntity<String> resetPassword(@PathVariable String email,
                                                @Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(email, req);
        return ResponseEntity.ok("Password reset successfully.");
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using a refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(authService.refreshToken(req));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate current session tokens")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token  = authHeader.substring(7);
        Long   userId = tokenProvider.getUserIdFromToken(token);
        authService.logout(authHeader, userId);
        return ResponseEntity.ok("Logged out successfully.");
    }

    /**
     * Internal endpoint used by the API Gateway to validate a JWT token.
     * Returns 200 OK with the user ID if valid, or 401 if invalid/blacklisted.
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token (called by gateway)")
    public ResponseEntity<Long> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long userId  = tokenProvider.getUserIdFromToken(token);
        return ResponseEntity.ok(userId);
    }
}
