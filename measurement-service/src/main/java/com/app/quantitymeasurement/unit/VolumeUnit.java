package com.app.quantitymeasurement.unit;

/**
 * Enum representing volume units such as Litre, Millilitre, and Gallon.
 * Implements IMeasurable to provide conversion logic to the base unit (Litre).
 */
public enum VolumeUnit implements IMeasurable {

	LITER(1.0),
    MILLILITER(0.001),
    GALLON(3.785);

    private final double conversionFactor;

    VolumeUnit(double conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    @Override
    public double getConversionFactor() {
        return conversionFactor;
    }

    @Override
    public double convertToBaseUnit(double value) {
        return value * conversionFactor;
    }

    @Override
    public double convertFromBaseUnit(double baseValue) {
        return baseValue / conversionFactor;
    }

    @Override
    public String getUnitName() {
        return name();
    }
}