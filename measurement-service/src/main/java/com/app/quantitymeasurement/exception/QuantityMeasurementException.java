package com.app.quantitymeasurement.exception;

/**
 * Custom exception class for Quantity Measurement errors.
 * This exception is thrown when invalid measurement operations or
 * unsupported unit conversions are attempted.
 */
public class QuantityMeasurementException extends RuntimeException {


	public QuantityMeasurementException(String message) {
        super(message);
    }

    public QuantityMeasurementException(String message, Throwable cause) {
        super(message, cause);
    }
}