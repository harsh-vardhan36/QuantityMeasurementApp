package com.app.quantitymeasurement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.quantitymeasurement.dto.AuthResponse;
import com.app.quantitymeasurement.dto.LoginRequest;
import com.app.quantitymeasurement.dto.RefreshTokenRequest;
import com.app.quantitymeasurement.dto.ResetPasswordRequest;
import com.app.quantitymeasurement.dto.SignUpRequest;
import com.app.quantitymeasurement.security.UserPrincipal;
import com.app.quantitymeasurement.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for all authentication operations.
 * All business logic is delegated to {@link AuthService}.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for registration, login, password management, and token operations")
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticates a user with email and password.
     * Returns a JWT access token and a refresh token.
     *
     * @param loginRequest the login credentials
     * @return {@link AuthResponse} containing accessToken, tokenType, and refreshToken
     */
    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    /**
     * Registers a new local user account.
     * Sends a welcome email asynchronously.
     *
     * @param signUpRequest the registration details
     * @return 200 OK with a success message
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<String> register(@Valid @RequestBody SignUpRequest signUpRequest) {
        authService.register(signUpRequest);
        return ResponseEntity.ok("User registered successfully! A welcome email has been sent.");
    }

    /**
     * Initiates the forgot-password flow by generating and emailing an OTP.
     *
     * @param email the email address of the account to reset
     * @return 200 OK with a confirmation message
     */
    @PostMapping("/forgotPassword/{email}")
    @Operation(summary = "Request a password reset OTP via email")
    public ResponseEntity<String> forgotPassword(@PathVariable String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok("Password reset OTP sent to " + email + ". Valid for 15 minutes.");
    }

    /**
     * Resets the user's password after validating the OTP from the email.
     *
     * @param email   the user's email address
     * @param request the OTP and new password
     * @return 200 OK with a success message
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
     * Issues a new access token using a valid refresh token.
     * The old refresh token is rotated (replaced) on each call.
     *
     * @param request containing the refresh token string
     * @return {@link AuthResponse} with new access token and refresh token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using a refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    /**
     * Logs out the currently authenticated user.
     * Blacklists the current access token and deletes the refresh token.
     *
     * @param authorizationHeader the Authorization header containing the Bearer token
     * @param currentUser         the authenticated user principal
     * @return 200 OK with a success message
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
