package com.app.quantitymeasurement.service;

import java.util.List;

import com.app.quantitymeasurement.dto.QuantityDTO;
import com.app.quantitymeasurement.dto.QuantityMeasurementDTO;

/**
 * Service interface for Quantity Measurement operations.
 * Defines methods for comparing, converting, and performing arithmetic
 * on quantities, as well as accessing and managing measurement history.
 */
public interface IQuantityMeasurementService {

    // Compare two quantities
    QuantityMeasurementDTO compare(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO);

    // Convert quantity to another unit
    QuantityMeasurementDTO convert(QuantityDTO quantityDTO, String targetUnit);

    // Add two quantities
    QuantityMeasurementDTO add(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO);

    // Add two quantities with a target unit
    QuantityMeasurementDTO add(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO, QuantityDTO targetQuantityDTO);

    // Subtract two quantities
    QuantityMeasurementDTO subtract(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO);

    // Subtract two quantities with a target unit
    QuantityMeasurementDTO subtract(QuantityDTO thisQuantityDTO, QuantityDTO thatQuantityDTO, QuantityDTO targetQuantityDTO);

    // Multiply quantity
    QuantityMeasurementDTO multiply(QuantityDTO quantityDTO, double factor);

    // Divide quantity by a scalar
    QuantityMeasurementDTO divide(QuantityDTO quantityDTO, double divisor);

    // Divide two quantities (ratio)
    QuantityMeasurementDTO divide(QuantityDTO q1, QuantityDTO q2);

    // Get operation history
    List<QuantityMeasurementDTO> getOperationHistory(String operation);

    // Get measurements by type
    List<QuantityMeasurementDTO> getMeasurementsByType(String measurementType);

    // Count operations
    long getOperationCount(String operation);

    // Get error history
    List<QuantityMeasurementDTO> getErrorHistory();

    // ── DELETE operations (present in monolithic, now added here) ─────────────

    /** Delete ALL history records for the current user (or all if admin). */
    void deleteAllHistory();

    /** Delete a single history record by its ID. */
    void deleteHistoryById(Long id);
}
