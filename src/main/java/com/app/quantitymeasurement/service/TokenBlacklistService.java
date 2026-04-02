package com.app.quantitymeasurement.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * In-memory token blacklist for implementing logout / token invalidation.
 *
 * <p>When a user logs out, the JTI (JWT ID) of their current access token
 * is added to this blacklist. The {@link com.app.quantitymeasurement.security.JwtAuthenticationFilter}
 * checks this list before accepting any token.</p>
 *
 * <p><strong>Note:</strong> This is an in-memory implementation. On server restart,
 * blacklisted tokens will be cleared. For production, consider a Redis-backed
 * implementation or storing JTIs with TTL in the database.</p>
 */
@Service
@Slf4j
public class TokenBlacklistService {

    /**
     * Thread-safe set storing blacklisted JWT IDs (JTIs).
     */
    private final Set<String> blacklistedJtis = ConcurrentHashMap.newKeySet();

    /**
     * Adds a JTI to the blacklist (called on logout).
     *
     * @param jti the JWT ID to blacklist
     */
    public void blacklist(String jti) {
        blacklistedJtis.add(jti);
        log.info("Token blacklisted. JTI: {}", jti);
    }

    /**
     * Checks whether a JTI has been blacklisted.
     *
     * @param jti the JWT ID to check
     * @return true if blacklisted (token is invalid), false if still valid
     */
    public boolean isBlacklisted(String jti) {
        return blacklistedJtis.contains(jti);
    }
}
