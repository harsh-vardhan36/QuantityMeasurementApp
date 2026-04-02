package com.app.quantitymeasurement.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.repository.UserRepository;

/**
 * Implementation of {@link UserDetailsService} that loads a {@link User} by ID or email.
 *
 * <p>This service is used by the {@link JwtAuthenticationFilter} to rehydrate
 * the authentication context after validating a JWT, using the user ID embedded
 * in the token claims.</p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Load a user by their email address (used during form-based login if needed).
     *
     * @param email the user's email
     * @return UserDetails for the found user
     * @throws UsernameNotFoundException if no user with the given email exists
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email : " + email)
                );
        return new UserPrincipal(user);
    }

    /**
     * Load a user by their internal database ID.
     * This is the primary method used by the JWT filter to restore the security context.
     *
     * @param id the user's database ID
     * @return UserDetails for the found user
     * @throws UsernameNotFoundException if no user with the given ID exists
     */
    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with id : " + id)
                );
        return new UserPrincipal(user);
    }
}
