package com.app.quantitymeasurement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model returned after a successful authentication.
 * Contains a short-lived JWT access token and a long-lived refresh token.
 */
@Data
//@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String refreshToken;

    /** Constructor for simple token-only responses (backward compatible). */
    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
    }

    /** Full constructor with refresh token. */
    public AuthResponse(String accessToken, String tokenType, String refreshToken) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.refreshToken = refreshToken;
    }
}
