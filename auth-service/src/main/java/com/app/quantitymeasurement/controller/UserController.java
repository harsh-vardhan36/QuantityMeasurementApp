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
 * REST Controller for authenticated user's own profile.
 *
 * Exposes:
 *   GET /api/user/me  — returns the currently logged-in user's details.
 *
 * This endpoint is routed from the API Gateway via the "user-profile-route"
 * to this auth-service. The Angular frontend calls it immediately after login
 * to populate the user session with firstName, lastName, email, and id.
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "User Profile", description = "Endpoints for the logged-in user's own profile")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Returns the full profile of the currently authenticated user.
     * Used by the Angular frontend after login to load user details.
     *
     * @param currentUser the authenticated user principal from the JWT
     * @return User entity with id, firstName, lastName, email
     */
    @GetMapping("/me")
    @Operation(summary = "Get current logged-in user's profile")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
        return ResponseEntity.ok(user);
    }
}
