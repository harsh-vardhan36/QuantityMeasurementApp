package com.quantitymeasurement.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for conversion history responses sent back to clients.
 */
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
