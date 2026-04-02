package com.app.quantitymeasurement.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.app.quantitymeasurement.entity.User;

/**
 * Implementation of both {@link UserDetails} and {@link OAuth2User} to represent
 * the authenticated user in the Spring Security context. Wraps the User entity
 * for compatibility with both JWT-based authentication and OAuth2 login.
 */
public class UserPrincipal implements OAuth2User, UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    /**
     * Constructs a UserPrincipal from a User entity.
     * All users are granted the ROLE_USER authority (no RBAC).
     *
     * @param user the authenticated User entity from the database
     */
    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * Creates a UserPrincipal from an OAuth2 login User entity,
     * also attaching the OAuth2 attributes map.
     *
     * @param user       the User entity
     * @param attributes the OAuth2 attributes from the provider
     * @return a UserPrincipal instance
     */
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = new UserPrincipal(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    /** Returns the internal database ID of the user. */
    public Long getId() {
        return id;
    }

    /** Returns the email of the user. */
    public String getEmail() {
        return email;
    }

    // ── UserDetails ──────────────────────────────────────────────────────────

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // ── OAuth2User ───────────────────────────────────────────────────────────

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }
}
