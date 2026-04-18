package com.app.quantitymeasurement.enums;

/**
 * Enum representing the types of operations available in the
 * Quantity Measurement application, such as addition, subtraction,
 * comparison, and conversion.
 */
public enum OperationType {

    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    COMPARE,
    CONVERT;

    // Optional: display name (for UI / response)
    public String getDisplayName() {
        return this.name().toLowerCase();
    }
}
