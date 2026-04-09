package com.app.authservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "OTP cannot be blank")
    private String otp;

    @NotBlank(message = "New password cannot be blank")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#^()_\\-+=])[A-Za-z\\d@$!%*?&#^()_\\-+=]{8,}$",
             message = "Password must be at least 8 characters with 1 uppercase, 1 lowercase, 1 number, 1 special character")
    private String newPassword;
}
