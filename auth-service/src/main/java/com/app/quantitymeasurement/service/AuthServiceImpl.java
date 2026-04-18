package com.app.quantitymeasurement.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.quantitymeasurement.client.UserServiceClient;
import com.app.quantitymeasurement.dto.ApiResponse;
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
import com.app.quantitymeasurement.repository.UserRepository;
import com.app.quantitymeasurement.security.JwtTokenProvider;
import com.app.quantitymeasurement.security.UserPrincipal;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of AuthService — contains all authentication business logic.
 * Matches monolithic app's full feature set:
 *   - Login with JWT + refresh token
 *   - Register (syncs to user-service)
 *   - Forgot password (OTP via email)
 *   - Reset password (OTP-based)
 *   - Refresh token (rotation)
 *   - Logout (JWT blacklist + delete refresh token)
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
    private UserServiceClient userServiceClient;

    @Autowired
    private EmailService emailService;

    @Value("${app.auth.reset-password-url:http://localhost:4200/reset-password}")
    private String resetPasswordUrl;

    // ── Login ─────────────────────────────────────────────────────────────────

    @Override
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
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

        // Send login notification email asynchronously
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
    public ApiResponse registerUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email address already in use.");
        }

        // 1. Create and save user identity locally (auth-service DB)
        User user = new User();
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setProvider(AuthProvider.local);
        user.setEmailVerified(false);

        User result = userRepository.save(user);
        log.info("Identity created in auth-service for: {}", result.getEmail());

        // 2. Sync with user-service (create profile)
        try {
            userServiceClient.createProfile(signUpRequest);
            log.info("Profile synchronized with user-service for: {}", signUpRequest.getEmail());
        } catch (Exception e) {
            log.error("Failed to sync profile with user-service: {}", e.getMessage());
        }

        // 3. Send welcome email asynchronously
        emailService.sendRegistrationEmail(result.getEmail(), result.getFirstName());

        return new ApiResponse(true, "User registered successfully! A welcome email has been sent.");
    }

    // ── Forgot Password ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Generate a 6-digit numeric OTP
        String otp = generateOtp();

        // Store BCrypt hash of OTP (never persist raw OTP)
        user.setResetPasswordToken(passwordEncoder.encode(otp));
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // Send OTP via email asynchronously
        emailService.sendPasswordResetEmail(email, otp);

        log.info("Password reset OTP sent to {}", email);
    }

    // ── Reset Password (OTP-based — matches monolithic /resetPassword/{email}) ─

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
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        // Validate OTP against stored BCrypt hash
        if (!passwordEncoder.matches(request.getOtp(), user.getResetPasswordToken())) {
            throw new BadRequestException("Invalid OTP. Please check your email and try again.");
        }

        // Update password and clear reset fields
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setLastPasswordResetDate(LocalDateTime.now());
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        // Invalidate any existing refresh tokens for security
        refreshTokenService.deleteByUserId(user.getId());

        log.info("Password reset successfully for {}", email);
    }

    // ── Reset Password (Token-based — matches auth-service /reset-password) ───

    @Override
    @Transactional
    public void resetPasswordByToken(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid password reset token."));

        if (user.getResetPasswordTokenExpiry() != null &&
                LocalDateTime.now().isAfter(user.getResetPasswordTokenExpiry())) {
            throw new BadRequestException("Password reset token has expired.");
        }

        if (!tokenProvider.validateToken(token)) {
            throw new BadRequestException("Invalid or expired password reset token.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLastPasswordResetDate(LocalDateTime.now());
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        refreshTokenService.deleteByUserId(user.getId());
        log.info("Password reset via token for user: {}", user.getEmail());
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

    /** Generates a cryptographically secure 6-digit OTP. */
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
