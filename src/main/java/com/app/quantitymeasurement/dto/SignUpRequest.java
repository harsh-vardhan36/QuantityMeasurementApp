package com.app.quantitymeasurement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request model for local user registration with strong validation rules.
 */
@Data
public class SignUpRequest {

    /**
     * Must start with an uppercase letter and contain only alphabets.
     */
    @NotBlank(message = "First name cannot be blank")
    @Pattern(
        regexp = "^[A-Z][a-zA-Z]*$",
        message = "First name must start with uppercase and contain only letters"
    )
    private String firstName;

    /**
     * Must start with an uppercase letter and contain only alphabets.
     */
    @NotBlank(message = "Last name cannot be blank")
    @Pattern(
        regexp = "^[A-Z][a-zA-Z]*$",
        message = "Last name must start with uppercase and contain only letters"
    )
    private String lastName;

    /**
     * Enhanced email validation: basic format + domain check.
     */
    @NotBlank(message = "Email cannot be blank")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$",
        message = "Email must be a valid email address with a proper domain"
    )
    private String email;

    /**
     * Password must be at least 8 characters, include 1 uppercase,
     * 1 lowercase, 1 digit, and 1 special character.
     */
    @NotBlank(message = "Password cannot be blank")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#^()_\\-+=])[A-Za-z\\d@$!%*?&#^()_\\-+=]{8,}$",
        message = "Password must be at least 8 characters and include 1 uppercase, 1 lowercase, 1 number, and 1 special character"
    )
    private String password;

    /**
     * Optional mobile number — exactly 10 digits.
     */
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
    private String mobileNo;
}
