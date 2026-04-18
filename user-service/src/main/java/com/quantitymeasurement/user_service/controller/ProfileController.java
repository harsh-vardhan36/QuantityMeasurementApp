package com.quantitymeasurement.user_service.controller;

import com.quantitymeasurement.user_service.entity.User;
import com.quantitymeasurement.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Profile sync endpoint — called by auth-service after user registration.
 * auth-service sends SignUpRequest (with mobileNo field) to sync profile here.
 */
@RestController
@RequestMapping("/api/user/profiles")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserRepository userRepository;

    /**
     * Called by auth-service via Feign after registration.
     * Creates user profile in user-service DB (quantitymeasurement_user_db).
     *
     * Auth-service sends SignUpRequest which has:
     *   firstName, lastName, email, password, mobileNo
     */
    @PostMapping("/sync")
    public ResponseEntity<?> syncProfile(@RequestBody Map<String, Object> request) {
        String email = (String) request.get("email");

        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        // Agar already exist karta hai to ignore karo (idempotent)
        if (userRepository.existsByEmail(email)) {
            log.info("Profile already exists for: {}, skipping sync", email);
            return ResponseEntity.ok(Map.of("message", "Profile already exists", "synced", false));
        }

        String firstName = (String) request.getOrDefault("firstName", "");
        String lastName  = (String) request.getOrDefault("lastName", "");
        String mobileNo  = (String) request.getOrDefault("mobileNo", "");

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .mobileNo(mobileNo)
                .build();

        User saved = userRepository.save(user);
        log.info("Profile synced for user: {} (id={})", email, saved.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Profile synced successfully",
                "synced", true,
                "userId", saved.getId()
        ));
    }
}