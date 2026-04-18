package com.app.quantitymeasurement.exception;

/**
 * Custom Exception for OAuth2 Authentication Processing.
 */
public class OAuth2AuthenticationProcessingException extends RuntimeException {
    public OAuth2AuthenticationProcessingException(String message) { super(message); }
    public OAuth2AuthenticationProcessingException(String message, Throwable cause) { super(message, cause); }
}
