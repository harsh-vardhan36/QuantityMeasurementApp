package com.app.quantitymeasurement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

import org.springframework.retry.annotation.EnableRetry;

/**
 * The entry point for the Quantity Measurement Application.
 * This class initializes the Spring Boot application and provides
 * OpenAPI documentation details for the Quantity Measurement API.
 *
 * <p>{@code @EnableAsync} activates asynchronous processing so that
 * methods annotated with {@code @Async} (e.g., email sending) run
 * in a separate thread pool without blocking the request thread.</p>
 *
 * <p>{@code @EnableFeignClients} scans for @FeignClient interfaces
 * and generates HTTP clients for interservice communication.</p>
 */
@SpringBootApplication
@EnableAsync
@EnableRetry
@EnableFeignClients
@OpenAPIDefinition(
    info = @Info(
        title = "Quantity Measurement API",
        version = "1.0.0",
        description = "REST API for quantity measurements with support for multiple unit types"
    )
)
public class QuantityMeasurementApplication {

    public static void main(String[] args) {
 
        SpringApplication.run(QuantityMeasurementApplication.class, args);
        
    }
}