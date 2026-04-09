package com.app.authservice.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.app.authservice.entity.User;

public class UserPrincipal implements OAuth2User, UserDetails {

    private final Long   id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public UserPrincipal(User user) {
        this.id          = user.getId();
        this.email       = user.getEmail();
        this.password    = user.getPassword();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal principal = new UserPrincipal(user);
        principal.setAttributes(attributes);
        return principal;
    }

    public Long getId()    { return id; }
    public String getEmail() { return email; }

    // ── UserDetails ──────────────────────────────────────────────────────────
    @Override public String getUsername()  { return email; }
    @Override public String getPassword()  { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }

    // ── OAuth2User ───────────────────────────────────────────────────────────
    @Override public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
    @Override public String getName() { return String.valueOf(id); }
}
