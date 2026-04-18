package com.app.quantitymeasurement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request model for resetting password.
 * Supports both OTP-based reset (monolithic style) and token-based reset.
 * The 'otp' field is used when frontend sends OTP from email.
 * The 'token' field is used when frontend sends a reset link token.
 */
@Data
public class ResetPasswordRequest {

    /** OTP received via email (6-digit code). Used for /resetPassword/{email} endpoint. */
    private String otp;

    /** JWT reset token. Used for /reset-password endpoint. */
    private String token;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;
}
