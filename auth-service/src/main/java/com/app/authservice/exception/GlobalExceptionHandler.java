package com.app.authservice.exception;

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

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

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
        err.status    = status.value();
        err.error     = error;
        err.message   = message;
        err.path      = request.getDescription(false).replace("uri=", "");
        return err;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest req) {
        List<String> msgs = ex.getBindingResult().getAllErrors()
                .stream().map(ObjectError::getDefaultMessage).collect(Collectors.toList());
        String message = String.join("; ", msgs);
        log.warn("Validation Error: {}", message);
        return new ResponseEntity<>(build(HttpStatus.BAD_REQUEST, "Validation Error", message, req), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, WebRequest req) {
        log.warn("Bad Request: {}", ex.getMessage());
        return new ResponseEntity<>(build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, WebRequest req) {
        log.warn("Not Found: {}", ex.getMessage());
        return new ResponseEntity<>(build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(TokenExpiredException ex, WebRequest req) {
        log.warn("Token Expired: {}", ex.getMessage());
        return new ResponseEntity<>(build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), req), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex, WebRequest req) {
        log.warn("Runtime Error: {}", ex.getMessage());
        return new ResponseEntity<>(build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobal(Exception ex, WebRequest req) {
        log.error("Internal Error: {}", ex.getMessage());
        return new ResponseEntity<>(build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), req), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
