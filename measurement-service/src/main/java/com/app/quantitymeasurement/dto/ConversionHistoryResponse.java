package com.app.quantitymeasurement.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionHistoryResponse {
    private Long id;
    private Long userId;
    private String type;
    private String fromUnit;
    private String toUnit;
    private double inputValue;
    private double outputValue;
    private LocalDateTime timestamp;
}
