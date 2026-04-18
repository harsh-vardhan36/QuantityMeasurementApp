package com.app.quantitymeasurement.unit;



/**
 * Interface for measurable units.
 * Defines the contract for converting values to and from a base unit,
 * and provides hooks for operation support validation.
 */
public interface IMeasurable {

    // Conversion methods 
    double convertToBaseUnit(double value);
    double convertFromBaseUnit(double baseValue);
    double getConversionFactor();
    String getUnitName();

    // Default arithmetic capability 
    default boolean supportsArithmetic() {
        return true;
    }

    // Default validation 
    default void validateOperationSupport(String operation) {
    }
}