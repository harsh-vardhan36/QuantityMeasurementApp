package com.app.quantitymeasurement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request model for resetting a password using an OTP.
 */
@Data
public class ResetPasswordRequest {

    /** The 6-digit OTP sent to the user's email. */
    @NotBlank(message = "OTP cannot be blank")
    private String otp;

    /** The new password must meet the strength requirements. */
    @NotBlank(message = "New password cannot be blank")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#^()_\\-+=])[A-Za-z\\d@$!%*?&#^()_\\-+=]{8,}$",
        message = "Password must be at least 8 characters and include 1 uppercase, 1 lowercase, 1 number, and 1 special character"
    )
    private String newPassword;
}
