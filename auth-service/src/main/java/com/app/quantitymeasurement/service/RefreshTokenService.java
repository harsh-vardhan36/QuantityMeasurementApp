package com.app.quantitymeasurement.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.quantitymeasurement.entity.RefreshToken;
import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.exception.BadRequestException;
import com.app.quantitymeasurement.repository.RefreshTokenRepository;
import com.app.quantitymeasurement.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service that manages the lifecycle of refresh tokens:
 * creation, validation, rotation, and deletion on logout.
 */
@Service
@Slf4j
public class RefreshTokenService {

    @Value("${app.auth.refresh-token-expiration-msec:2592000000}")
    private long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Creates (or replaces) a refresh token for the given user.
     * Old tokens for the same user are deleted before creating a new one (rotation).
     *
     * @param userId the ID of the user
     * @return the newly created {@link RefreshToken}
     */
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Rotate: delete any existing refresh token for this user
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Finds a refresh token by its token string.
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Verifies that the given refresh token has not expired.
     * If it has expired, the token is deleted from the DB and an exception is thrown.
     */
    @Transactional
    public RefreshToken verifyExpiry(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            log.warn("Refresh token [{}] has expired. Deleted from DB.", token.getToken());
            throw new BadRequestException("Refresh token has expired. Please log in again.");
        }
        return token;
    }

    /**
     * Deletes all refresh tokens for a given user (used during logout).
     */
    @Transactional
    public void deleteByUserId(Long userId) {
        userRepository.findById(userId).ifPresent(refreshTokenRepository::deleteByUser);
    }
}
