package com.app.authservice.service;

import com.app.authservice.dto.*;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);
    void         register(SignUpRequest signUpRequest);
    void         forgotPassword(String email);
    void         resetPassword(String email, ResetPasswordRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void         logout(String bearerToken, Long userId);
}
