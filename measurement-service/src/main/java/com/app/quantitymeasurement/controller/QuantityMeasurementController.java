package com.app.quantitymeasurement.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.quantitymeasurement.dto.QuantityInputDTO;
import com.app.quantitymeasurement.dto.QuantityMeasurementDTO;
import com.app.quantitymeasurement.exception.QuantityMeasurementException;
import com.app.quantitymeasurement.service.IQuantityMeasurementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Quantity Measurement operations.
 *
 * Endpoints match the monolithic QuantityMeasurementApp exactly:
 *   POST   /api/user/quantities/compare
 *   POST   /api/user/quantities/convert
 *   POST   /api/user/quantities/add
 *   POST   /api/user/quantities/add-with-target-unit
 *   POST   /api/user/quantities/subtract
 *   POST   /api/user/quantities/subtract-with-target-unit
 *   POST   /api/user/quantities/multiply
 *   POST   /api/user/quantities/divide
 *   GET    /api/user/quantities/history/operation/{operation}
 *   GET    /api/user/quantities/history/type/{type}
 *   GET    /api/user/quantities/count/{operation}
 *   GET    /api/user/quantities/history/errored
 *   DELETE /api/user/quantities/history/all      ← was MISSING, now added
 *   DELETE /api/user/quantities/history/{id}     ← was MISSING, now added
 */
@RestController
@RequestMapping("/api/user/quantities")
@Tag(name = "Quantity Measurements", description = "REST API for quantity measurement operations")
@RequiredArgsConstructor
@Slf4j
public class QuantityMeasurementController {

    private final IQuantityMeasurementService service;

    // ── COMPARE ──────────────────────────────────────────────────────────────

    @PostMapping("/compare")
    @Operation(summary = "Compare two quantities")
    public ResponseEntity<?> performComparison(@Valid @RequestBody QuantityInputDTO input) {
        log.info("Received compare request payload={}", input);
        QuantityMeasurementDTO response = service.compare(input.getThisQuantityDTO(), input.getThatQuantityDTO());
        if (response.isError()) throw new QuantityMeasurementException(response.getErrorMessage());
        return ResponseEntity.ok(response);
    }

    // ── CONVERT ──────────────────────────────────────────────────────────────

    @PostMapping("/convert")
    @Operation(summary = "Convert quantity")
    public ResponseEntity<?> performConversion(@Valid @RequestBody QuantityInputDTO input) {
        log.info("Received convert request payload={}", input);
        QuantityMeasurementDTO response = service.convert(
                input.getThisQuantityDTO(), input.getThatQuantityDTO().getUnit());
        if (response.isError()) throw new QuantityMeasurementException(response.getErrorMessage());
        return ResponseEntity.ok(response);
    }

    // ── ADD ──────────────────────────────────────────────────────────────────

    @PostMapping("/add")
    @Operation(summary = "Add two quantities")
    public ResponseEntity<?> performAddition(@Valid @RequestBody QuantityInputDTO input) {
        QuantityMeasurementDTO response = service.add(
                input.getThisQuantityDTO(), input.getThatQuantityDTO());
        if (response.isError()) throw new QuantityMeasurementException(response.getErrorMessage());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-with-target-unit")
    @Operation(summary = "Add with target unit")
    public ResponseEntity<?> performAdditionWithTargetUnit(@Valid @RequestBody QuantityInputDTO input) {
        QuantityMeasurementDTO response = service.add(
                input.getThisQuantityDTO(), input.getThatQuantityDTO(), input.getTargetQuantityDTO());
        if (response.isError()) throw new QuantityMeasurementException(response.getErrorMessage());
        return ResponseEntity.ok(response);
    }

    // ── SUBTRACT ─────────────────────────────────────────────────────────────

    @PostMapping("/subtract")
    @Operation(summary = "Subtract two quantities")
    public ResponseEntity<?> performSubtraction(@Valid @RequestBody QuantityInputDTO input) {
        QuantityMeasurementDTO response = service.subtract(
                input.getThisQuantityDTO(), input.getThatQuantityDTO());
        if (response.isError()) throw new QuantityMeasurementException(response.getErrorMessage());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/subtract-with-target-unit")
    @Operation(summary = "Subtract with target unit")
    public ResponseEntity<?> performSubtractionWithTargetUnit(@Valid @RequestBody QuantityInputDTO input) {
        QuantityMeasurementDTO response = service.subtract(
                input.getThisQuantityDTO(), input.getThatQuantityDTO(), input.getTargetQuantityDTO());
        if (response.isError()) throw new QuantityMeasurementException(response.getErrorMessage());
        return ResponseEntity.ok(response);
    }

    // ── MULTIPLY ─────────────────────────────────────────────────────────────

    @PostMapping("/multiply")
    @Operation(summary = "Multiply a quantity by a factor")
    public ResponseEntity<?> performMultiplication(@Valid @RequestBody QuantityInputDTO input) {
        QuantityMeasurementDTO response = service.multiply(
                input.getThisQuantityDTO(), input.getThatQuantityDTO().getValue());
        if (response.isError()) throw new QuantityMeasurementException(response.getErrorMessage());
        return ResponseEntity.ok(response);
    }

    // ── DIVIDE ───────────────────────────────────────────────────────────────

    @PostMapping("/divide")
    @Operation(summary = "Divide two quantities")
    public ResponseEntity<?> performDivision(@Valid @RequestBody QuantityInputDTO input) {
        QuantityMeasurementDTO response = service.divide(
                input.getThisQuantityDTO(), input.getThatQuantityDTO());
        if (response.isError()) throw new QuantityMeasurementException(response.getErrorMessage());
        return ResponseEntity.ok(response);
    }

    // ── HISTORY (READ) ────────────────────────────────────────────────────────

    @GetMapping("/history/operation/{operation}")
    @Operation(summary = "Get history by operation type")
    public ResponseEntity<List<?>> getOperationHistory(@PathVariable String operation) {
        return ResponseEntity.ok(service.getOperationHistory(operation));
    }

    @GetMapping("/history/type/{type}")
    @Operation(summary = "Get history by measurement type")
    public ResponseEntity<List<?>> getHistoryByType(@PathVariable String type) {
        return ResponseEntity.ok(service.getMeasurementsByType(type));
    }

    @GetMapping("/count/{operation}")
    @Operation(summary = "Get count of a specific operation")
    public ResponseEntity<?> getOperationCount(@PathVariable String operation) {
        return ResponseEntity.ok(service.getOperationCount(operation));
    }

    @GetMapping("/history/errored")
    @Operation(summary = "Get all errored operations")
    public ResponseEntity<List<?>> getErroredOperations() {
        return ResponseEntity.ok(service.getErrorHistory());
    }

    // ── HISTORY (DELETE) — was MISSING in microservice, now added ─────────────

    /**
     * Deletes ALL history records for the current user.
     * Matches monolithic: DELETE /api/user/quantities/history/all
     */
    @DeleteMapping("/history/all")
    @Operation(summary = "Delete all history")
    public ResponseEntity<?> deleteAllHistory() {
        service.deleteAllHistory();
        return ResponseEntity.ok("History deleted successfully");
    }

    /**
     * Deletes a single history record by ID.
     * Matches monolithic: DELETE /api/user/quantities/history/{id}
     */
    @DeleteMapping("/history/{id}")
    @Operation(summary = "Delete history by id")
    public ResponseEntity<?> deleteHistoryById(@PathVariable Long id) {
        service.deleteHistoryById(id);
        return ResponseEntity.ok("Record deleted successfully");
    }
}
