package com.app.quantitymeasurement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversionHistoryRequest {
    private String type;       // LENGTH, WEIGHT, TEMPERATURE, VOLUME
    private String fromUnit;
    private String toUnit;
    private double inputValue;
    private double outputValue;
}
