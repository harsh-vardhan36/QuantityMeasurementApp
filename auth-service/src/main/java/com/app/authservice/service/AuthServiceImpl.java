package com.app.authservice.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.authservice.dto.*;
import com.app.authservice.entity.*;
import com.app.authservice.enums.AuthProvider;
import com.app.authservice.exception.*;
import com.app.authservice.repository.UserRepository;
import com.app.authservice.security.*;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository        userRepository;
    @Autowired private PasswordEncoder       passwordEncoder;
    @Autowired private JwtTokenProvider      tokenProvider;
    @Autowired private RefreshTokenService   refreshTokenService;
    @Autowired private TokenBlacklistService tokenBlacklistService;
    @Autowired private EmailService          emailService;

    // ── Login ──────────────────────────────────────────────────────────────────

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        RefreshToken refreshToken   = refreshTokenService.createRefreshToken(userPrincipal.getId());

        // Send optional login notification
        userRepository.findById(userPrincipal.getId()).ifPresent(user ->
                emailService.sendLoginNotificationEmail(user.getEmail(), user.getFirstName()));

        log.info("User [{}] logged in successfully.", loginRequest.getEmail());
        return new AuthResponse(jwt, "Bearer", refreshToken.getToken());
    }

    // ── Register ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void register(SignUpRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new BadRequestException("Email address is already in use: " + req.getEmail());
        }

        User user = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .mobileNo(req.getMobileNo())
                .provider(AuthProvider.local)
                .emailVerified(false)
                .build();

        userRepository.save(user);
        emailService.sendRegistrationEmail(user.getEmail(), user.getFirstName());

        log.info("New user registered: {}", req.getEmail());
    }

    // ── Forgot Password ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        String otp = generateOtp();
        user.setResetPasswordToken(passwordEncoder.encode(otp));
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(email, otp);
        log.info("Password reset OTP sent to {}", email);
    }

    // ── Reset Password ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void resetPassword(String email, ResetPasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.getResetPasswordToken() == null || user.getResetPasswordTokenExpiry() == null) {
            throw new BadRequestException("No password reset request found for this email.");
        }

        if (LocalDateTime.now().isAfter(user.getResetPasswordTokenExpiry())) {
            user.setResetPasswordToken(null);
            user.setResetPasswordTokenExpiry(null);
            userRepository.save(user);
            throw new TokenExpiredException("OTP has expired. Please request a new one.");
        }

        if (!passwordEncoder.matches(request.getOtp(), user.getResetPasswordToken())) {
            throw new BadRequestException("Invalid OTP. Please check your email and try again.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        refreshTokenService.deleteByUserId(user.getId());
        log.info("Password reset successfully for {}", email);
    }

    // ── Refresh Token ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Refresh token not found. Please log in again."));

        refreshTokenService.verifyExpiry(refreshToken);

        String newJwt              = tokenProvider.generateTokenFromUserId(refreshToken.getUser().getId());
        RefreshToken newRefreshTok = refreshTokenService.createRefreshToken(refreshToken.getUser().getId());

        log.info("Token refreshed for user ID {}", refreshToken.getUser().getId());
        return new AuthResponse(newJwt, "Bearer", newRefreshTok.getToken());
    }

    // ── Logout ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void logout(String bearerToken, Long userId) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String jwt = bearerToken.substring(7);
            if (tokenProvider.validateToken(jwt)) {
                tokenBlacklistService.blacklist(tokenProvider.getJtiFromToken(jwt));
            }
        }
        refreshTokenService.deleteByUserId(userId);
        log.info("User [ID={}] logged out.", userId);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String generateOtp() {
        int otp = 100000 + new SecureRandom().nextInt(900000);
        return String.valueOf(otp);
    }
}
