package com.app.quantitymeasurement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.exception.ResourceNotFoundException;
import com.app.quantitymeasurement.repository.UserRepository;
import com.app.quantitymeasurement.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for user profile management.
 *
 * <p>Exposes an endpoint that returns the currently authenticated user's
 * profile information. Requires a valid JWT in the Authorization header.</p>
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "User Profile", description = "APIs for managing the authenticated user's profile")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Returns the profile of the currently authenticated user.
     *
     * <p>The user ID is extracted from the JWT via the {@link UserPrincipal}
     * populated in the security context by the {@code JwtAuthenticationFilter}.</p>
     *
     * @param currentUser the authenticated user principal from the security context
     * @return a {@link ResponseEntity} with the user's details
     */
    @GetMapping("/me")
    @Operation(summary = "Get the current authenticated user's profile")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
        return ResponseEntity.ok(user);
    }
}
