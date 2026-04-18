package com.quantitymeasurement.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for incoming conversion history save requests from measurement-service.
 */
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
