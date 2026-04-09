package com.app.authservice.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * In-memory JWT blacklist for logout / token invalidation.
 * For production, replace with a Redis-backed store with TTL matching token expiry.
 */
@Service
@Slf4j
public class TokenBlacklistService {

    private final Set<String> blacklistedJtis = ConcurrentHashMap.newKeySet();

    public void blacklist(String jti) {
        blacklistedJtis.add(jti);
        log.info("Token blacklisted. JTI: {}", jti);
    }

    public boolean isBlacklisted(String jti) {
        return blacklistedJtis.contains(jti);
    }
}
