package com.quantitymeasurement.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a conversion history record.
 * Each record stores the details of a unit conversion performed by a user.
 * measurement-service sends these records via Feign after each conversion.
 */
@Entity
@Table(name = "conversion_history", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Conversion type: LENGTH, WEIGHT, TEMPERATURE, VOLUME */
    @Column(nullable = false)
    private String type;

    @Column(name = "from_unit", nullable = false)
    private String fromUnit;

    @Column(name = "to_unit", nullable = false)
    private String toUnit;

    @Column(name = "input_value", nullable = false)
    private double inputValue;

    @Column(name = "output_value", nullable = false)
    private double outputValue;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
