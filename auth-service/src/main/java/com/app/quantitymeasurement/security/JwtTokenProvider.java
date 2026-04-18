package com.app.quantitymeasurement.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

/**
 * Utility class for generating and validating JSON Web Tokens (JWT).
 * Used for stateless authentication.
 *
 * Tokens include JTI (JWT ID) claim to support logout/blacklisting.
 */
@Service
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.auth.token-secret}")
    private String tokenSecret;

    @Value("${app.auth.token-expiration-msec}")
    private long tokenExpirationMsec;

    private SecretKey getSigningKey() {
        byte[] keyBytes = tokenSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT with enriched claims for an authenticated principal.
     */
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return buildToken(userPrincipal.getId(),
                userPrincipal.getEmail(),
                userPrincipal.getFirstName(),
                userPrincipal.getLastName(),
                userPrincipal.getImageUrl());
    }

    /**
     * Generates a JWT directly from a user ID (used after token refresh).
     */
    public String generateTokenFromUserId(Long userId) {
        return Jwts.builder()
                .subject(Long.toString(userId))
                .id(UUID.randomUUID().toString())   // JTI for blacklisting
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpirationMsec))
                .signWith(getSigningKey())
                .compact();
    }

    private String buildToken(Long userId, String email, String firstName, String lastName, String imageUrl) {
        return Jwts.builder()
                .subject(Long.toString(userId))
                .claim("email", email)
                .claim("name", firstName + " " + lastName)
                .claim("firstName", firstName)
                .claim("lastName", lastName)
                .claim("picture", imageUrl)
                .id(UUID.randomUUID().toString())   // JTI for blacklisting
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpirationMsec))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generates a short-lived JWT for password reset (15 minutes).
     */
    public String generatePasswordResetToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900000))
                .signWith(getSigningKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extracts the JTI (JWT ID) from a token — used for blacklisting on logout.
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
     * Returns remaining validity (ms) of the token.
     */
    public long getTokenExpirationMs(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }
}
