package com.app.quantitymeasurement.security;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Utility component for creating and validating JSON Web Tokens (JWTs).
 *
 * <p>Tokens are signed using HMAC-SHA512 with a secret key defined in
 * {@code application.properties}. The token subject is the authenticated user's
 * database ID. A unique JTI (JWT ID) is embedded to support logout/blacklisting.</p>
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.auth.tokenSecret}")
    private String tokenSecret;

    @Value("${app.auth.tokenExpirationMsec}")
    private long tokenExpirationMsec;

    /**
     * Generates a signed JWT for an authenticated principal.
     *
     * @param authentication the Spring Security Authentication object
     * @return a compact, URL-safe JWT string
     */
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return buildToken(userPrincipal.getId());
    }

    /**
     * Generates a JWT directly from a user ID (used after password reset / refresh).
     *
     * @param userId the user's database ID
     * @return a compact, URL-safe JWT string
     */
    public String generateTokenFromUserId(Long userId) {
        return buildToken(userId);
    }

    private String buildToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tokenExpirationMsec);

        return Jwts.builder()
                .subject(Long.toString(userId))
                .id(UUID.randomUUID().toString())   // JTI for blacklisting
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the user ID from a valid JWT.
     *
     * @param token the JWT string
     * @return the user's database ID as a Long
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extracts the JTI (JWT ID) from a token, used for blacklisting on logout.
     *
     * @param token the JWT string
     * @return the JTI string
     */
    public String getJtiFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getId();
    }

    /**
     * Returns the remaining validity duration (ms) of the token.
     *
     * @param token the JWT string
     * @return milliseconds until expiry
     */
    public long getTokenExpirationMs(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    /**
     * Validates a JWT token — checks signature and expiry.
     *
     * @param authToken the JWT string to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty");
        }
        return false;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(tokenSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
