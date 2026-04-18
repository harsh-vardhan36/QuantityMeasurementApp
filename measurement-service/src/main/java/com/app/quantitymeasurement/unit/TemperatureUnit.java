package com.app.quantitymeasurement.unit;

import java.util.function.Function;

/**
 * Enum representing temperature units: Celsius, Fahrenheit, and Kelvin.
 * Implements IMeasurable with specialized (non-linear) conversion functions.
 *
 * Arithmetic operations work on the base unit (Celsius):
 *   e.g. ADD: convertToBase(v1) + convertToBase(v2) → convertFromBase(result)
 * This is consistent with how Length, Weight, etc. work in their base units.
 */
public enum TemperatureUnit implements IMeasurable {

    CELSIUS(
            c -> c,
            c -> c
    ),

    FAHRENHEIT(
            f -> (f - 32) * 5.0 / 9.0,
            c -> (c * 9.0 / 5.0) + 32
    ),

    KELVIN(
            k -> k - 273.15,
            c -> c + 273.15
    );

    private final Function<Double, Double> toCelsius;
    private final Function<Double, Double> fromCelsius;

    TemperatureUnit(Function<Double, Double> toCelsius,
                    Function<Double, Double> fromCelsius) {
        this.toCelsius = toCelsius;
        this.fromCelsius = fromCelsius;
    }

    @Override
    public double convertToBaseUnit(double value) {
        return toCelsius.apply(value);
    }

    @Override
    public double convertFromBaseUnit(double baseValue) {
        return fromCelsius.apply(baseValue);
    }

    @Override
    public double getConversionFactor() {
        throw new UnsupportedOperationException(
                "getConversionFactor() is not supported for TemperatureUnit — conversion is non-linear"
        );
    }

    @Override
    public String getUnitName() {
        return name();
    }

    /** Temperature NOW supports arithmetic — operations run in the base unit (Celsius). */
    @Override
    public boolean supportsArithmetic() {
        return true;
    }

    /** All standard arithmetic operations (ADD, SUBTRACT, MULTIPLY, DIVIDE) are allowed. */
    @Override
    public void validateOperationSupport(String operation) {
        switch (operation.toUpperCase()) {
            case "ADD":
            case "SUBTRACT":
            case "MULTIPLY":
            case "DIVIDE":
                return; // supported
            default:
                throw new UnsupportedOperationException(
                        "Temperature does not support operation: " + operation
                );
        }
    }
}