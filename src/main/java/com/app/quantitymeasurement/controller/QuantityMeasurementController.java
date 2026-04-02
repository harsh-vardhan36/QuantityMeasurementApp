package com.app.quantitymeasurement.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.quantitymeasurement.dto.QuantityInputDTO;
import com.app.quantitymeasurement.dto.QuantityMeasurementDTO;
import com.app.quantitymeasurement.service.IQuantityMeasurementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Quantity Measurement operations.
 * This class provides endpoints for performing comparisons, conversions,
 * arithmetic operations (addition, subtraction, multiplication, division),
 * and retrieving measurement history.
 */
@RestController
@RequestMapping("/api/user/quantities")
@Tag(name = "Quantity Measurements", description = "REST API for quantity measurement operations")
@RequiredArgsConstructor
@Slf4j
public class QuantityMeasurementController {

	private final IQuantityMeasurementService service;

	// ===================== COMPARE =====================
	@PostMapping("/compare")
	@Operation(summary = "Compare two quantities")
	public ResponseEntity<?> performComparison(@Valid @RequestBody QuantityInputDTO input) {

		QuantityMeasurementDTO response = service.compare(input.getThisQuantityDTO(), input.getThatQuantityDTO());

		if (response.isError()) {
			throw new com.app.quantitymeasurement.exception.QuantityMeasurementException(response.getErrorMessage());
		}
		
		return ResponseEntity.ok(response);
	}

	// ===================== CONVERT =====================
	@PostMapping("/convert")
	@Operation(summary = "Convert quantity")
	public ResponseEntity<?> performConversion(@Valid @RequestBody QuantityInputDTO input) {

		QuantityMeasurementDTO response = service.convert(input.getThisQuantityDTO(),
				input.getThatQuantityDTO().getUnit());

		if (response.isError()) {
			throw new com.app.quantitymeasurement.exception.QuantityMeasurementException(response.getErrorMessage());
		}

		return ResponseEntity.ok(response);
	}

	// ===================== ADD =====================
	@PostMapping("/add")
	@Operation(summary = "Add two quantities")
	public ResponseEntity<?> performAddition(@Valid @RequestBody QuantityInputDTO input) {

		QuantityMeasurementDTO response = service.add(input.getThisQuantityDTO(), input.getThatQuantityDTO());

		if (response.isError()) {
			throw new com.app.quantitymeasurement.exception.QuantityMeasurementException(response.getErrorMessage());
		}

		return ResponseEntity.ok(response);
	}

	// ===================== ADD WITH TARGET =====================
	@PostMapping("/add-with-target-unit")
	@Operation(summary = "Add with target unit")
	public ResponseEntity<?> performAdditionWithTargetUnit(@Valid @RequestBody QuantityInputDTO input) {

		QuantityMeasurementDTO response = service.add(input.getThisQuantityDTO(), input.getThatQuantityDTO(),
				input.getTargetQuantityDTO());
		
		if (response.isError()) {
			throw new com.app.quantitymeasurement.exception.QuantityMeasurementException(response.getErrorMessage());
		}
		
		return ResponseEntity.ok(response);
	}

	// ===================== SUBTRACT =====================
	@PostMapping("/subtract")
	@Operation(summary = "Subtract two quantities")
	public ResponseEntity<?> performSubtraction(@Valid @RequestBody QuantityInputDTO input) {

		QuantityMeasurementDTO response = service.subtract(input.getThisQuantityDTO(), input.getThatQuantityDTO());
		
		if (response.isError()) {
			throw new com.app.quantitymeasurement.exception.QuantityMeasurementException(response.getErrorMessage());
		}
		
		return ResponseEntity.ok(response);
	}

	// ===================== SUBTRACT WITH TARGET =====================
	@PostMapping("/subtract-with-target-unit")
	@Operation(summary = "Subtract with target unit")
	public ResponseEntity<?> performSubtractionWithTargetUnit(@Valid @RequestBody QuantityInputDTO input) {

		QuantityMeasurementDTO response = service.subtract(input.getThisQuantityDTO(), input.getThatQuantityDTO(),
				input.getTargetQuantityDTO());
		
		if (response.isError()) {
			throw new com.app.quantitymeasurement.exception.QuantityMeasurementException(response.getErrorMessage());
		}
		
		return ResponseEntity.ok(response);

	}

	// ===================== MULTIPLY =====================
	@PostMapping("/multiply")
	@Operation(summary = "Multiply a quantity by a factor")
	public ResponseEntity<?> performMultiplication(@Valid @RequestBody QuantityInputDTO input) {

		QuantityMeasurementDTO response = service.multiply(input.getThisQuantityDTO(),
				input.getThatQuantityDTO().getValue());
		
		if (response.isError()) {
			throw new com.app.quantitymeasurement.exception.QuantityMeasurementException(response.getErrorMessage());
		}
		
		return ResponseEntity.ok(response);

	}

	// ===================== DIVIDE =====================
	@PostMapping("/divide")
	@Operation(summary = "Divide two quantities")
	public ResponseEntity<?> performDivision(@Valid @RequestBody QuantityInputDTO input) {

		QuantityMeasurementDTO response = service.divide(input.getThisQuantityDTO(),
				input.getThatQuantityDTO());
		
		if (response.isError()) {
			throw new com.app.quantitymeasurement.exception.QuantityMeasurementException(response.getErrorMessage());
		}

		return ResponseEntity.ok(response);
	}

	// ===================== HISTORY BY OPERATION =====================
	@GetMapping("/history/operation/{operation}")
	public ResponseEntity<List<?>> getOperationHistory(@PathVariable String operation) {

		return ResponseEntity.ok(service.getOperationHistory(operation));
	}

	// ===================== HISTORY BY TYPE =====================
	@GetMapping("/history/type/{type}")
	public ResponseEntity<List<?>> getHistoryByType(@PathVariable String type) {

		return ResponseEntity.ok(service.getMeasurementsByType(type));
	}

	// ===================== COUNT =====================
	@GetMapping("/count/{operation}")
	public ResponseEntity<?> getOperationCount(@PathVariable String operation) {

		return ResponseEntity.ok(service.getOperationCount(operation));
	}

	// ===================== ERROR HISTORY =====================
	@GetMapping("/history/errored")
	public ResponseEntity<List<?>> getErroredOperations() {

		return ResponseEntity.ok(service.getErrorHistory());
	}
}