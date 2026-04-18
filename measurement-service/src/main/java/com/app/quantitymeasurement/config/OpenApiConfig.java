package com.app.quantitymeasurement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

/**
 * OpenAPI Configuration for Measurement Service.
 * This configuration enables Swagger UI to work properly and documents the API endpoints.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configure OpenAPI documentation for the Measurement Service.
     * Includes JWT security scheme definition.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Quantity Measurement Service API")
                        .version("1.0")
                        .description("REST API for quantity measurement operations including comparison, conversion, and arithmetic operations")
                        .contact(new Contact()
                                .name("Quantity Measurement Team")
                                .email("support@quantitymeasurement.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Development server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("API Gateway")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token")));
    }
}