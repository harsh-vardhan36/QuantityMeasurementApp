# UC17: Spring Framework Integration - REST Services & JPA

## Overview

UC17 upgrades the Quantity Measurement Application from a standalone JDBC-based system (UC16) to a Spring Boot-powered RESTful application.

This transformation introduces:
- REST APIs for interaction
- Spring Data JPA for persistence
- Dependency Injection and transaction management
- Production-ready architecture

The goal is to move toward enterprise-grade backend development while preserving all existing business logic from UC1–UC16.

---

## Key Features

### Spring Boot Integration
- Auto-configuration and simplified setup
- Embedded Tomcat server (no external server required)

### RESTful API
- Exposes endpoints using `@RestController`
- Supports JSON/XML responses
- Proper HTTP methods: GET, POST, PUT, DELETE

### Spring Data JPA
- Eliminates JDBC boilerplate
- Automatic ORM mapping
- Repository interfaces instead of manual SQL

### Layered Architecture
- Controller Layer → Handles HTTP requests  
- Service Layer → Business logic  
- Repository Layer → Database interaction  

### DTO-Based Communication
- `QuantityInputDTO` → Input payload  
- `QuantityMeasurementDTO` → Response object  

### Exception Handling
- Centralized using `@ControllerAdvice`
- Clean API error responses with HTTP status codes

### Swagger API Documentation
- Interactive API documentation
- Uses `@Operation` and `@Tag`

### Testing Support
- Unit and Integration testing
- `MockMvc` for REST API testing

### Monitoring
- Spring Boot Actuator for:
  - Health checks
  - Metrics

---

## What Changed from UC16?

UC16 used JDBC. UC17 replaces it with the Spring ecosystem.

| UC16 (Before) | UC17 (Now) |
|--------------|-----------|
| Manual JDBC code | Spring Data JPA |
| SQL queries | Repository methods |
| Manual DI | Spring DI (`@Autowired`) |
| No REST support | REST APIs |
| Manual JSON handling | Automatic serialization |
| Manual transaction handling | Declarative (optional) |
| Hard to scale | Microservice-ready |

---

## Problems in UC16 (Solved in UC17)

- Too much boilerplate (ResultSet, Connection handling)
- Manual transaction management
- No REST API support
- Difficult testing
- Tight coupling
- No framework ecosystem support

UC17 resolves all of these using Spring Boot.

---

---

## Important Components

### Controller Layer
- Handles API requests
- Uses:
  - `@RestController`
  - `@RequestMapping("/api/v1/quantities")`
  - `@PostMapping`, `@GetMapping`

### Service Layer
- Contains business logic
- Annotated with `@Service`
- Uses repository for DB operations

### Repository Layer
- Extends `JpaRepository`
- No SQL required

---

## Sample API Endpoints

| Method | Endpoint | Description |
|--------|----------|------------|
| POST | `/compare` | Compare two quantities |
| POST | `/convert` | Convert units |
| POST | `/add` | Add quantities |
| GET | `/history/{operation}` | Get operation history |
| GET | `/count/{operation}` | Get operation count |

---

## Example Request

```json
{
  "value1": 10,
  "unit1": "FEET",
  "value2": 120,
  "unit2": "INCH"
}

