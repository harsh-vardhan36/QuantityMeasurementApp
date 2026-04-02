package com.app.quantitymeasurement.service;

import com.app.quantitymeasurement.dto.AuthResponse;
import com.app.quantitymeasurement.dto.LoginRequest;
import com.app.quantitymeasurement.dto.RefreshTokenRequest;
import com.app.quantitymeasurement.dto.ResetPasswordRequest;
import com.app.quantitymeasurement.dto.SignUpRequest;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    /**
     * Authenticates a user and returns JWT + refresh token.
     */
    AuthResponse login(LoginRequest loginRequest);

    /**
     * Registers a new local user account.
     */
    void register(SignUpRequest signUpRequest);

    /**
     * Initiates a password reset by sending an OTP to the user's email.
     */
    void forgotPassword(String email);

    /**
     * Resets the user's password after validating the OTP.
     */
    void resetPassword(String email, ResetPasswordRequest request);

    /**
     * Issues a new access token from a valid refresh token.
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Invalidates the access token (adds JTI to blacklist) and deletes the refresh token.
     */
    void logout(String bearerToken, Long userId);
}
