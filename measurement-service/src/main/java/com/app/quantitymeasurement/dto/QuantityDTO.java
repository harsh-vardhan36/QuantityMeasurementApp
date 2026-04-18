package com.app.quantitymeasurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "A quantity with a value and unit")
@AllArgsConstructor
@NoArgsConstructor
/**
 * Data Transfer Object for a single quantity.
 * Contains the numerical value, the unit name, and the measurement type.
 * Includes validation logic to ensure the unit matches the measurement type.
 */
public class QuantityDTO {

    @NotNull(message = "Value cannot be null")
    @Schema(example = "1.0")
    private Double value;

    @NotEmpty(message = "Unit cannot be empty")
    @Schema(example = "FEET", allowableValues = {
            "FEET", "INCHES", "YARDS", "CENTIMETERS",
            "LITER", "MILLILITER", "GALLON",
            "MILLIGRAM", "GRAM", "KILOGRAM", "POUND", "TONNE",
            "CELSIUS", "FAHRENHEIT", "KELVIN"
    })
    private String unit;

    @NotNull(message = "Measurement type cannot be null")
    @Pattern(
        regexp = "LengthUnit|VolumeUnit|WeightUnit|TemperatureUnit",
        message = "Measurement type must be one of: LengthUnit, VolumeUnit, WeightUnit, TemperatureUnit"
    )
    @Schema(example = "LengthUnit", allowableValues = {
            "LengthUnit", "VolumeUnit", "WeightUnit", "TemperatureUnit"
    })
    private String measurementType;
    
    /**
     * Custom validation: unit must match measurement type
     */
    @AssertTrue(message = "Unit must be valid for the specified measurement type")
    public boolean isValidUnit() {
        if (unit == null || measurementType == null) return false;

        try {
            switch (measurementType) {
                case "LengthUnit":
                    LengthUnit.valueOf(unit);
                    break;
                case "VolumeUnit":
                    VolumeUnit.valueOf(unit);
                    break;
                case "WeightUnit":
                    WeightUnit.valueOf(unit);
                    break;
                case "TemperatureUnit":
                    TemperatureUnit.valueOf(unit);
                    break;
                default:
                    return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    // Enums (as given by mentor)

	public enum LengthUnit {
        FEET, INCHES, YARDS, CENTIMETERS
    }

    public enum VolumeUnit {
        LITER, MILLILITER, GALLON
    }

    public enum WeightUnit {
        MILLIGRAM, GRAM, KILOGRAM, POUND, TONNE
    }

    public enum TemperatureUnit {
        CELSIUS, FAHRENHEIT, KELVIN
    }
}