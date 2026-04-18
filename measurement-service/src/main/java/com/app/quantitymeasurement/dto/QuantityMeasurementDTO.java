package com.app.quantitymeasurement.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.app.quantitymeasurement.entity.QuantityMeasurementEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
/**
 * Data Transfer Object for quantity measurement results.
 * This class captures the input values, the operation performed, 
 * the calculated result, and any error information.
 */
public class QuantityMeasurementDTO {

    private Long id;                      // ← needed for frontend delete
    private LocalDateTime createdAt;      // ← needed for history timestamps

    private double thisValue;
    private String thisUnit;
    private String thisMeasurementType;

    private double thatValue;
    private String thatUnit;
    private String thatMeasurementType;

    private String operation;

    private String resultString;
    private double resultValue;
    private String resultUnit;
    private String resultMeasurementType;

    private String errorMessage;

    @JsonProperty("error")
    private boolean error;

    /**
     * Entity → DTO
     */
    public static QuantityMeasurementDTO from(QuantityMeasurementEntity entity) {

        if (entity == null) return null;

        QuantityMeasurementDTO dto = new QuantityMeasurementDTO();

        dto.setId(entity.getId());                      // ← map id
        dto.setCreatedAt(entity.getCreatedAt());        // ← map timestamp

        dto.setThisValue(entity.getThisValue());
        dto.setThisUnit(entity.getThisUnit());
        dto.setThisMeasurementType(entity.getThisMeasurementType());

        dto.setThatValue(entity.getThatValue());
        dto.setThatUnit(entity.getThatUnit());
        dto.setThatMeasurementType(entity.getThatMeasurementType());

        dto.setOperation(entity.getOperation());

        dto.setResultString(entity.getResultString());
        dto.setResultValue(entity.getResultValue());
        dto.setResultUnit(entity.getResultUnit());
        dto.setResultMeasurementType(entity.getResultMeasurementType());

        dto.setError(entity.isError());
        dto.setErrorMessage(entity.getErrorMessage());

        return dto;
    }

    /**
     * DTO → Entity
     */
    public QuantityMeasurementEntity toEntity() {

        QuantityMeasurementEntity entity = new QuantityMeasurementEntity();

        entity.setThisValue(thisValue);
        entity.setThisUnit(thisUnit);
        entity.setThisMeasurementType(thisMeasurementType);

        entity.setThatValue(thatValue);
        entity.setThatUnit(thatUnit);
        entity.setThatMeasurementType(thatMeasurementType);

        entity.setOperation(operation);

        entity.setResultString(resultString);
        entity.setResultValue(resultValue);
        entity.setResultUnit(resultUnit);
        entity.setResultMeasurementType(resultMeasurementType);

        entity.setError(error); // boolean setter
        entity.setErrorMessage(errorMessage);

        return entity;
    }

    /**
     * List<Entity> → List<DTO>
     */
    public static List<QuantityMeasurementDTO> fromList(List<QuantityMeasurementEntity> entities) {
        return entities.stream()
                .map(QuantityMeasurementDTO::from)
                .collect(Collectors.toList());
    }

}