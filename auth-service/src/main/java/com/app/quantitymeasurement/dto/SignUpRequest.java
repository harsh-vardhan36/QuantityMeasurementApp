package com.app.quantitymeasurement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Sign Up Request DTO.
 */
public class SignUpRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Invalid mobile number")
    private String mobile;

    public SignUpRequest() {}

    public SignUpRequest(String firstName, String lastName, String email, String password, String mobileNo) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.mobile = mobileNo;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getMobileNo() { return mobile; }
    public void setMobileNo(String mobileNo) { this.mobile = mobileNo; }

    // Builder Pattern
    public static class SignUpRequestBuilder {
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private String mobile;

        public SignUpRequestBuilder firstName(String firstName) { this.firstName = firstName; return this; }
        public SignUpRequestBuilder lastName(String lastName) { this.lastName = lastName; return this; }
        public SignUpRequestBuilder email(String email) { this.email = email; return this; }
        public SignUpRequestBuilder password(String password) { this.password = password; return this; }
        public SignUpRequestBuilder mobileNo(String mobileNo) { this.mobile = mobileNo; return this; }

        public SignUpRequest build() {
            return new SignUpRequest(firstName, lastName, email, password, mobile);
        }
    }

    public static SignUpRequestBuilder builder() {
        return new SignUpRequestBuilder();
    }
}
