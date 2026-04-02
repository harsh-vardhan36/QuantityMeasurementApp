package com.app.quantitymeasurement.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.quantitymeasurement.dto.AuthResponse;
import com.app.quantitymeasurement.dto.LoginRequest;
import com.app.quantitymeasurement.dto.RefreshTokenRequest;
import com.app.quantitymeasurement.dto.ResetPasswordRequest;
import com.app.quantitymeasurement.dto.SignUpRequest;
import com.app.quantitymeasurement.entity.RefreshToken;
import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.enums.AuthProvider;
import com.app.quantitymeasurement.exception.BadRequestException;
import com.app.quantitymeasurement.exception.ResourceNotFoundException;
import com.app.quantitymeasurement.exception.TokenExpiredException;
import com.app.quantitymeasurement.repository.UserRepository;
import com.app.quantitymeasurement.security.JwtTokenProvider;
import com.app.quantitymeasurement.security.UserPrincipal;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link AuthService} — contains all authentication business logic.
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private EmailService emailService;

    // ── Login ─────────────────────────────────────────────────────────────────

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
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());

        // Optional: send login notification email
        User user = userRepository.findById(userPrincipal.getId()).orElse(null);
        if (user != null) {
            emailService.sendLoginNotificationEmail(user.getEmail(), user.getFirstName());
        }

        log.info("User [{}] logged in successfully.", loginRequest.getEmail());
        return new AuthResponse(jwt, "Bearer", refreshToken.getToken());
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void register(SignUpRequest signUpRequest) {
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            throw new BadRequestException("Email address is already in use: " + signUpRequest.getEmail());
        }

        User user = User.builder()
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .mobileNo(signUpRequest.getMobileNo())
                .provider(AuthProvider.local)
                .emailVerified(false)
                .build();

        userRepository.save(user);

        // Send welcome email asynchronously
        emailService.sendRegistrationEmail(user.getEmail(), user.getFirstName());

        log.info("New user registered: {}", signUpRequest.getEmail());
    }

    // ── Forgot Password ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Generate a 6-digit numeric OTP
        String otp = generateOtp();

        // Store a BCrypt hash of the OTP so the raw OTP is never persisted
        user.setResetPasswordToken(passwordEncoder.encode(otp));
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // Send OTP via email asynchronously
        emailService.sendPasswordResetEmail(email, otp);

        log.info("Password reset OTP sent to {}", email);
    }

    // ── Reset Password ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void resetPassword(String email, ResetPasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Validate token existence
        if (user.getResetPasswordToken() == null || user.getResetPasswordTokenExpiry() == null) {
            throw new BadRequestException("No password reset request found for this email.");
        }

        // Validate expiry
        if (LocalDateTime.now().isAfter(user.getResetPasswordTokenExpiry())) {
            user.setResetPasswordToken(null);
            user.setResetPasswordTokenExpiry(null);
            userRepository.save(user);
            throw new TokenExpiredException("OTP has expired. Please request a new one.");
        }

        // Validate OTP (compare against stored BCrypt hash)
        if (!passwordEncoder.matches(request.getOtp(), user.getResetPasswordToken())) {
            throw new BadRequestException("Invalid OTP. Please check your email and try again.");
        }

        // Update password and clear reset fields
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        // Invalidate any existing refresh tokens for security
        refreshTokenService.deleteByUserId(user.getId());

        log.info("Password reset successfully for {}", email);
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Refresh token not found. Please log in again."));

        refreshTokenService.verifyExpiry(refreshToken);

        // Issue new access token
        String newJwt = tokenProvider.generateTokenFromUserId(refreshToken.getUser().getId());

        // Rotate refresh token
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(refreshToken.getUser().getId());

        log.info("Token refreshed for user ID {}", refreshToken.getUser().getId());
        return new AuthResponse(newJwt, "Bearer", newRefreshToken.getToken());
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void logout(String bearerToken, Long userId) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String jwt = bearerToken.substring(7);
            if (tokenProvider.validateToken(jwt)) {
                String jti = tokenProvider.getJtiFromToken(jwt);
                tokenBlacklistService.blacklist(jti);
            }
        }
        refreshTokenService.deleteByUserId(userId);
        log.info("User [ID={}] logged out.", userId);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Generates a cryptographically secure 6-digit OTP.
     */
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
