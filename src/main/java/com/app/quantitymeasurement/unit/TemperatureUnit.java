package com.app.quantitymeasurement.unit;

import java.util.function.Function;

/**
 * Enum representing temperature units such as Celsius, Fahrenheit, and Kelvin.
 * Implements IMeasurable with specialized conversion functions.
 * Note: Temperature does not support standard arithmetic operations (addition/subtraction).
 */
public enum TemperatureUnit implements IMeasurable {

    CELSIUS(
            c -> c,
            c -> c
    ),

    FAHRENHEIT(
            f -> (f - 32) * 5 / 9,
            c -> (c * 9 / 5) + 32
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

    // Temperature does NOT support arithmetic
    @Override
    public boolean supportsArithmetic() {
        return false;
    }

    @Override
    public void validateOperationSupport(String operation) {
        throw new UnsupportedOperationException(
                "Temperature does not support arithmetic operation: " + operation
        );
    }
}