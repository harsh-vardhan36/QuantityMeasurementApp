package com.app.quantitymeasurement.entity;

import java.time.LocalDateTime;

import com.app.quantitymeasurement.enums.AuthProvider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA Entity representing an authenticated User in the system.
 * Supports both local (email/password) and OAuth2 (Google) authentication.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** User's first name. */
    private String firstName;

    /** User's last name. */
    private String lastName;

    /** User's email address; must be unique and cannot be null. */
    @Email
    @NotNull
    @Column(nullable = false)
    private String email;

    /** URL of the user's profile picture (OAuth2 only). */
    private String imageUrl;

    /** Whether the email address has been verified. */
    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    /** BCrypt-encoded password (null for OAuth2-only users). */
    @Column
    private String password;

    /** The authentication provider used (e.g., google, local). */
    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    /** The ID assigned by the OAuth2 provider (e.g., Google subject ID). */
    private String providerId;

    /** User's mobile number. */
    private String mobileNo;

    /** Token used to authorize a password reset. */
    @Column
    private String resetPasswordToken;

    /** Expiry time for the password reset token. */
    @Column
    private LocalDateTime resetPasswordTokenExpiry;
}
