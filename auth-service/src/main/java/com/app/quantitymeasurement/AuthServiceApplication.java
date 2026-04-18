package com.app.quantitymeasurement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main entry point for the Auth Service.
 * This service handles authentication (JWT/OAuth2) and Identity management.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.app.quantitymeasurement.client")
@ComponentScan(basePackages = "com.app.quantitymeasurement")
@EnableJpaRepositories(basePackages = "com.app.quantitymeasurement.repository")
@EntityScan(basePackages = "com.app.quantitymeasurement.entity")
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
