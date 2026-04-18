package com.app.quantitymeasurement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.quantitymeasurement.dto.ApiResponse;
import com.app.quantitymeasurement.dto.AuthResponse;
import com.app.quantitymeasurement.dto.LoginRequest;
import com.app.quantitymeasurement.dto.RefreshTokenRequest;
import com.app.quantitymeasurement.dto.ResetPasswordRequest;
import com.app.quantitymeasurement.dto.SignUpRequest;
import com.app.quantitymeasurement.service.AuthService;
import com.app.quantitymeasurement.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST Controller for all authentication operations.
 *
 * URL contracts match the monolithic QuantityMeasurementApp exactly so that
 * the Angular frontend requires ZERO changes:
 *
 *   POST /api/auth/login
 *   POST /api/auth/register
 *   POST /api/auth/forgotPassword/{email}
 *   POST /api/auth/resetPassword/{email}
 *   POST /api/auth/refresh
 *   POST /api/auth/logout
 *
 * Legacy / alternative endpoints kept for backward compatibility:
 *   POST /api/auth/signup          → alias for /register
 *   POST /api/auth/forgot-password → alias for /forgotPassword
 *   POST /api/auth/reset-password  → token-based reset
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for registration, login, password management, and token operations")
public class AuthController {

    @Autowired
    private AuthService authService;

    // ── Login ─────────────────────────────────────────────────────────────────

    /**
     * Authenticates a user with email and password.
     * Returns a JWT access token and a refresh token.
     */
    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    // ── Register ──────────────────────────────────────────────────────────────

    /**
     * Registers a new local user account.
     * Sends a welcome email asynchronously.
     * URL: /api/auth/register  (matches monolithic)
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<String> register(@Valid @RequestBody SignUpRequest signUpRequest) {
        authService.registerUser(signUpRequest);
        return ResponseEntity.ok("User registered successfully! A welcome email has been sent.");
    }

    /**
     * Alias for /register — kept for backward compatibility.
     */
    @PostMapping("/signup")
    @Operation(summary = "Register a new user account (alias for /register)")
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authService.registerUser(signUpRequest));
    }

    // ── Forgot Password ───────────────────────────────────────────────────────

    /**
     * Initiates the forgot-password flow by generating and emailing a 6-digit OTP.
     * URL: /api/auth/forgotPassword/{email}  (matches monolithic)
     */
    @PostMapping("/forgotPassword/{email}")
    @Operation(summary = "Request a password reset OTP via email")
    public ResponseEntity<String> forgotPassword(@PathVariable String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok("Password reset OTP sent to " + email + ". Valid for 15 minutes.");
    }

    /**
     * Alias with request body — kept for backward compatibility.
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset OTP (body variant)")
    public ResponseEntity<ApiResponse> forgotPasswordBody(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        authService.forgotPassword(email);
        return ResponseEntity.ok(new ApiResponse(true, "Password reset OTP sent to " + email + ". Valid for 15 minutes."));
    }

    // ── Reset Password ────────────────────────────────────────────────────────

    /**
     * Resets the user's password after validating the OTP from the email.
     * URL: /api/auth/resetPassword/{email}  (matches monolithic)
     */
    @PostMapping("/resetPassword/{email}")
    @Operation(summary = "Reset password using OTP received via email")
    public ResponseEntity<String> resetPassword(
            @PathVariable String email,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(email, request);
        return ResponseEntity.ok("Password reset successfully. Please log in with your new password.");
    }

    /**
     * Token-based reset — kept for backward compatibility.
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using JWT reset-link token")
    public ResponseEntity<ApiResponse> resetPasswordByToken(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPasswordByToken(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new ApiResponse(true, "Password has been reset successfully."));
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    /**
     * Issues a new access token using a valid refresh token.
     * The old refresh token is rotated (replaced) on each call.
     * URL: /api/auth/refresh  (matches monolithic)
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using a refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    /**
     * Logs out the currently authenticated user.
     * Blacklists the current access token and deletes the refresh token.
     * URL: /api/auth/logout  (matches monolithic)
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate the current session tokens")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization") String authorizationHeader,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        authService.logout(authorizationHeader, currentUser.getId());
        return ResponseEntity.ok("Logged out successfully.");
    }
}
