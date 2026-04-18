package com.quantitymeasurement.user_service.controller;

import com.quantitymeasurement.user_service.dto.CreateUserRequest;
import com.quantitymeasurement.user_service.entity.User;
import com.quantitymeasurement.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

/**
 * REST Controller for user profile management in user-service.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;

    /**
     * Get all users.
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get a user by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new user profile.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating user profile for: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("User already exists with email: {}", request.getEmail());
            throw new IllegalArgumentException("User already exists with email: " + request.getEmail());
        }

        User user = User.builder()
                .firstName(capitalize(request.getFirstName()))
                .lastName(capitalize(request.getLastName()))
                .email(request.getEmail())
                .mobileNo(request.getMobileNo())
                .build();

        return userRepository.save(user);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Get a user by email.
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
