package com.app.quantitymeasurement.service;

import com.app.quantitymeasurement.dto.ApiResponse;
import com.app.quantitymeasurement.dto.AuthResponse;
import com.app.quantitymeasurement.dto.LoginRequest;
import com.app.quantitymeasurement.dto.RefreshTokenRequest;
import com.app.quantitymeasurement.dto.ResetPasswordRequest;
import com.app.quantitymeasurement.dto.SignUpRequest;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    /** Authenticate a user and return JWT + refresh token. */
    AuthResponse authenticateUser(LoginRequest loginRequest);

    /** Register a new user and sync with user-service. */
    ApiResponse registerUser(SignUpRequest signUpRequest);

    /** Initiate password reset flow by email (OTP-based). */
    void forgotPassword(String email);

    /** Reset password using OTP received via email. */
    void resetPassword(String email, ResetPasswordRequest request);

    /** Reset password using a JWT reset-link token. */
    void resetPasswordByToken(String token, String newPassword);

    /** Refresh the access token using a valid refresh token. */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /** Logout: blacklist access token + delete refresh token. */
    void logout(String bearerToken, Long userId);
}
