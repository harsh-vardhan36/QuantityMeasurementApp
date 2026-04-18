package com.app.quantitymeasurement.unit;

/**
 * Enum representing weight units such as Kilogram, Gram, Milligram, Pound, and Tonne.
 * Implements IMeasurable to provide conversion logic to the base unit (Gram).
 */
public enum WeightUnit implements IMeasurable {

	/* base KiloGram
	 * KILOGRAM(1.0), GRAM(0.001), POUND(0.453592);
	 */
	
	KILOGRAM(1000.0),
    GRAM(1.0),           // base Gram
    MILLIGRAM(0.001),
    POUND(453.592),
    TONNE(1000000.0);
	
    private final double conversionFactor;

    WeightUnit(double conversionFactor) {
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