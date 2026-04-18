package com.app.quantitymeasurement.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.app.quantitymeasurement.dto.ApiResponse;
import com.app.quantitymeasurement.dto.SignUpRequest;

/**
 * Feign client to communicate with the user-service.
 */
@FeignClient(name = "user-service")
public interface UserServiceClient {

    /**
     * Creates a profile for the new user in the user-service.
     */
    @PostMapping("/api/user/profiles/sync")
    ApiResponse createProfile(@RequestBody SignUpRequest signUpRequest);
}
