package com.app.authservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignUpRequest {

    @NotBlank(message = "First name cannot be blank")
    @Pattern(regexp = "^[A-Z][a-zA-Z]*$",
             message = "First name must start with uppercase and contain only letters")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Pattern(regexp = "^[A-Z][a-zA-Z]*$",
             message = "Last name must start with uppercase and contain only letters")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$",
             message = "Email must be a valid email address with a proper domain")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#^()_\\-+=])[A-Za-z\\d@$!%*?&#^()_\\-+=]{8,}$",
             message = "Password must be at least 8 characters with 1 uppercase, 1 lowercase, 1 number, 1 special character")
    private String password;

    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
    private String mobileNo;
}
