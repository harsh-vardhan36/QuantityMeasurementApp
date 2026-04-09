package com.app.authservice.dto;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private String refreshToken;
}
