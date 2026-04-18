package com.app.quantitymeasurement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request model for refreshing an access token.
 */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;
}
