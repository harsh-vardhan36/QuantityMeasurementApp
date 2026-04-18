package com.app.quantitymeasurement.service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis-backed token blacklist for implementing logout / token invalidation.
 *
 * <p>When a user logs out, the JTI (JWT ID) of their current access token
 * is stored in Redis with a TTL equal to the remaining JWT lifetime.
 * Redis automatically removes the key when it expires — no cleanup needed.</p>
 *
 * <p>Falls back to an in-memory set if Redis is unavailable, so the service
 * continues working even without a Redis connection (graceful degradation).</p>
 */
@Service
@Slf4j
public class TokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "blacklist:jti:";

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    /** Fallback in-memory store if Redis is not available */
    private final Set<String> inMemoryFallback = ConcurrentHashMap.newKeySet();

    @Value("${app.auth.token-expiration-msec:864000000}")
    private long tokenExpirationMsec;

    /**
     * Blacklists a JTI in Redis with a TTL matching the JWT expiration time.
     * If Redis is not reachable, falls back to in-memory storage.
     *
     * @param jti the JWT ID to blacklist
     */
    public void blacklist(String jti) {
        blacklist(jti, tokenExpirationMsec);
    }

    /**
     * Blacklists a JTI with a custom TTL in milliseconds.
     *
     * @param jti       the JWT ID to blacklist
     * @param ttlMillis how long to keep the entry before auto-expiry
     */
    public void blacklist(String jti, long ttlMillis) {
        if (redisTemplate != null) {
            try {
                String key = BLACKLIST_KEY_PREFIX + jti;
                redisTemplate.opsForValue().set(key, "1", Duration.ofMillis(ttlMillis));
                log.info("Token blacklisted in Redis. JTI: {}", jti);
                return;
            } catch (Exception e) {
                log.warn("Redis unavailable, falling back to in-memory blacklist. Error: {}", e.getMessage());
            }
        }
        inMemoryFallback.add(jti);
        log.info("Token blacklisted in-memory (Redis fallback). JTI: {}", jti);
    }

    /**
     * Checks whether a JTI has been blacklisted.
     *
     * @param jti the JWT ID to check
     * @return true if blacklisted (token should be rejected), false if valid
     */
    public boolean isBlacklisted(String jti) {
        if (redisTemplate != null) {
            try {
                return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_KEY_PREFIX + jti));
            } catch (Exception e) {
                log.warn("Redis unavailable for blacklist check, using in-memory fallback. Error: {}", e.getMessage());
            }
        }
        return inMemoryFallback.contains(jti);
    }
}

