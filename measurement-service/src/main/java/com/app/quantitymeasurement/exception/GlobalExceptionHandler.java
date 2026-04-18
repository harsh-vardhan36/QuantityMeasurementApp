package com.app.quantitymeasurement.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * Global Exception Handler for the Quantity Measurement Application.
 * Intercepts exceptions thrown by controllers and returns structured,
 * consistent JSON error responses with appropriate HTTP status codes.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── Error Response Structure ───────────────────────────────────────────────

    static class ErrorResponse {
        public LocalDateTime timestamp;
        public int status;
        public String error;
        public String message;
        public String path;
    }

    private ErrorResponse build(HttpStatus status, String error, String message, WebRequest request) {
        ErrorResponse err = new ErrorResponse();
        err.timestamp = LocalDateTime.now();
        err.status = status.value();
        err.error = error;
        err.message = message;
        err.path = request.getDescription(false).replace("uri=", "");
        return err;
    }

    // ── 1. Validation Errors (400) ────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        List<String> errMsg = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.toList());

        String message = String.join("; ", errMsg);
        ErrorResponse error = build(HttpStatus.BAD_REQUEST, "Validation Error", message, request);
        log.warn("Validation Error: {}", message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ── 2. Bad Request (400) ─────────────────────────────────────────────────

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException ex, WebRequest request) {

        ErrorResponse error = build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
        log.warn("Bad Request: {}", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ── 3. Resource Not Found (404) ───────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {

        ErrorResponse error = build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
        log.warn("Not Found: {}", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // ── 4. Token Expired (401) ────────────────────────────────────────────────

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(
            TokenExpiredException ex, WebRequest request) {

        ErrorResponse error = build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request);
        log.warn("Token Expired: {}", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // ── 5. Custom Domain Exception (400) ──────────────────────────────────────

    @ExceptionHandler(QuantityMeasurementException.class)
    public ResponseEntity<ErrorResponse> handleQuantityException(
            QuantityMeasurementException ex, WebRequest request) {

        ErrorResponse error = build(HttpStatus.BAD_REQUEST, "Quantity Measurement Error", ex.getMessage(), request);
        log.warn("Quantity Error: {}", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ── 6. Arithmetic Exception (422) ─────────────────────────────────────────

    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<ErrorResponse> handleArithmeticException(
            ArithmeticException ex, WebRequest request) {

        ErrorResponse error = build(HttpStatus.UNPROCESSABLE_ENTITY, "Arithmetic Error", ex.getMessage(), request);
        log.error("Arithmetic Error: {}", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // ── 7. Illegal Argument Exception (400) ───────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        ErrorResponse error = build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
        log.warn("Illegal Argument: {}", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ── 8. Generic Runtime Exception (400) ───────────────────────────────────

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        ErrorResponse error = build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
        log.warn("Runtime Error: {}", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ── 8. Catch-All (500) ────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {

        ErrorResponse error = build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), request);
        log.error("Global Error: {}", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}