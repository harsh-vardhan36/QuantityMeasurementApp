package com.app.quantitymeasurement.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.quantitymeasurement.dto.QuantityDTO;
import com.app.quantitymeasurement.dto.QuantityMeasurementDTO;
import com.app.quantitymeasurement.entity.QuantityMeasurementEntity;
import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.repository.QuantityMeasurementRepository;
import com.app.quantitymeasurement.repository.UserRepository;
import com.app.quantitymeasurement.security.UserPrincipal;
import com.app.quantitymeasurement.unit.IMeasurable;
import com.app.quantitymeasurement.unit.LengthUnit;
import com.app.quantitymeasurement.unit.TemperatureUnit;
import com.app.quantitymeasurement.unit.VolumeUnit;
import com.app.quantitymeasurement.unit.WeightUnit;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of IQuantityMeasurementService.
 * Handles the logic for unit conversions, comparisons, and arithmetic operations
 * while persisting each operation to the database via QuantityMeasurementRepository.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuantityMeasurementServiceImpl implements IQuantityMeasurementService {

    private static final double COMPARISON_TOLERANCE = 0.0001;

    private final QuantityMeasurementRepository repository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
            return userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        throw new RuntimeException("Unauthorized user: " + auth);
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }

    // ================== COMPARE ==================
    @Override
    public QuantityMeasurementDTO compare(QuantityDTO q1, QuantityDTO q2) {

        QuantityMeasurementEntity entity = new QuantityMeasurementEntity();

        boolean result = performComparison(q1, q2);

        entity.setThisValue(q1.getValue());
        entity.setThisUnit(q1.getUnit());
        entity.setThisMeasurementType(q1.getMeasurementType());

        entity.setThatValue(q2.getValue());
        entity.setThatUnit(q2.getUnit());
        entity.setThatMeasurementType(q2.getMeasurementType());

        entity.setOperation("COMPARE");
        entity.setResultString(result ? "true" : "false");
        entity.setError(false);
        entity.setUser(getCurrentUser());

        repository.save(entity);
        return QuantityMeasurementDTO.from(entity);
    }

    // ================== CONVERT ==================
    @Override
    public QuantityMeasurementDTO convert(QuantityDTO quantity, String targetUnit) {

        QuantityMeasurementEntity entity = new QuantityMeasurementEntity();

        double result = convertValue(quantity, targetUnit);

        entity.setThisValue(quantity.getValue());
        entity.setThisUnit(quantity.getUnit());
        entity.setThisMeasurementType(quantity.getMeasurementType());

        entity.setThatValue(0.0);
        entity.setThatUnit("N/A");
        entity.setThatMeasurementType("N/A");

        entity.setOperation("CONVERT");
        entity.setResultValue(result);
        entity.setResultUnit(targetUnit);
        entity.setResultMeasurementType(quantity.getMeasurementType());
        entity.setError(false);
        entity.setUser(getCurrentUser());

        repository.save(entity);
        return QuantityMeasurementDTO.from(entity);
    }

    // ================== ADD ==================
    @Override
    public QuantityMeasurementDTO add(QuantityDTO q1, QuantityDTO q2) {
        return performArithmetic(q1, q2, null, "ADD", Double::sum);
    }

    @Override
    public QuantityMeasurementDTO add(QuantityDTO q1, QuantityDTO q2, QuantityDTO target) {
        return performArithmetic(q1, q2, target, "ADD", Double::sum);
    }

    // ================== SUBTRACT ==================
    @Override
    public QuantityMeasurementDTO subtract(QuantityDTO q1, QuantityDTO q2) {
        return performArithmetic(q1, q2, null, "SUBTRACT", (a, b) -> a - b);
    }

    @Override
    public QuantityMeasurementDTO subtract(QuantityDTO q1, QuantityDTO q2, QuantityDTO target) {
        return performArithmetic(q1, q2, target, "SUBTRACT", (a, b) -> a - b);
    }

    // ================== MULTIPLY ==================
    @Override
    public QuantityMeasurementDTO multiply(QuantityDTO q, double factor) {

        QuantityMeasurementEntity entity = new QuantityMeasurementEntity();

        IMeasurable u1 = getUnitEnum(q.getMeasurementType(), q.getUnit());
        u1.validateOperationSupport("MULTIPLY");
        double result = q.getValue() * factor;

        entity.setThisValue(q.getValue());
        entity.setThisUnit(q.getUnit());
        entity.setThisMeasurementType(q.getMeasurementType());

        entity.setThatValue(factor);
        entity.setThatUnit("FACTOR");
        entity.setThatMeasurementType("Scalar");

        entity.setOperation("MULTIPLY");
        entity.setResultValue(result);
        entity.setResultUnit(q.getUnit());
        entity.setResultMeasurementType(q.getMeasurementType());
        entity.setError(false);
        entity.setUser(getCurrentUser());

        repository.save(entity);
        return QuantityMeasurementDTO.from(entity);
    }

    // ================== DIVIDE ==================
    @Override
    public QuantityMeasurementDTO divide(QuantityDTO q, double divisor) {

        QuantityMeasurementEntity entity = new QuantityMeasurementEntity();

        if (divisor == 0) {
            throw new ArithmeticException("Divide by zero");
        }
        IMeasurable u1 = getUnitEnum(q.getMeasurementType(), q.getUnit());
        u1.validateOperationSupport("DIVIDE");
        double result = q.getValue() / divisor;

        entity.setThisValue(q.getValue());
        entity.setThisUnit(q.getUnit());
        entity.setThisMeasurementType(q.getMeasurementType());

        entity.setThatValue(divisor);
        entity.setThatUnit("FACTOR");
        entity.setThatMeasurementType("Scalar");

        entity.setOperation("DIVIDE");
        entity.setResultValue(result);
        entity.setResultUnit(q.getUnit());
        entity.setResultMeasurementType(q.getMeasurementType());
        entity.setError(false);
        entity.setUser(getCurrentUser());

        repository.save(entity);
        return QuantityMeasurementDTO.from(entity);
    }

    // ================== DIVIDE (RATIO) ==================
    @Override
    public QuantityMeasurementDTO divide(QuantityDTO q1, QuantityDTO q2) {
        QuantityMeasurementEntity entity = new QuantityMeasurementEntity();

        if (!q1.getMeasurementType().equals(q2.getMeasurementType())) {
            throw new IllegalArgumentException("Cannot divide different measurement types");
        }

        IMeasurable u1 = getUnitEnum(q1.getMeasurementType(), q1.getUnit());
        IMeasurable u2 = getUnitEnum(q2.getMeasurementType(), q2.getUnit());

        double base1 = u1.convertToBaseUnit(q1.getValue());
        double base2 = u2.convertToBaseUnit(q2.getValue());

        if (base2 == 0) {
            throw new ArithmeticException("Divide by zero");
        }

        double ratio = base1 / base2;

        entity.setThisValue(q1.getValue());
        entity.setThisUnit(q1.getUnit());
        entity.setThisMeasurementType(q1.getMeasurementType());

        entity.setThatValue(q2.getValue());
        entity.setThatUnit(q2.getUnit());
        entity.setThatMeasurementType(q2.getMeasurementType());

        entity.setOperation("DIVIDE");
        entity.setResultValue(ratio);
        entity.setResultUnit("RATIO");
        entity.setResultMeasurementType("Scalar");
        entity.setError(false);
        entity.setUser(getCurrentUser());

        repository.save(entity);
        return QuantityMeasurementDTO.from(entity);
    }

    // ================== HISTORY ==================
    @Override
    public List<QuantityMeasurementDTO> getOperationHistory(String operation) {
        if (isAdmin()) {
            return QuantityMeasurementDTO.fromList(repository.findByOperation(operation));
        }
        return QuantityMeasurementDTO.fromList(
                repository.findByUserAndOperation(getCurrentUser(), operation)
        );
    }

    @Override
    public List<QuantityMeasurementDTO> getMeasurementsByType(String type) {
        if (isAdmin()) {
            return QuantityMeasurementDTO.fromList(repository.findByThisMeasurementType(type));
        }
        return QuantityMeasurementDTO.fromList(
                repository.findByUserAndThisMeasurementType(getCurrentUser(), type)
        );
    }

    @Override
    public long getOperationCount(String operation) {
        if (isAdmin()) {
            return repository.countByOperationAndIsErrorFalse(operation);
        }
        return repository.countByUserAndOperationAndIsErrorFalse(getCurrentUser(), operation);
    }

    @Override
    public List<QuantityMeasurementDTO> getErrorHistory() {
        if (isAdmin()) {
            return QuantityMeasurementDTO.fromList(repository.findByIsErrorTrue());
        }
        return QuantityMeasurementDTO.fromList(
                repository.findByUserAndIsErrorTrue(getCurrentUser())
        );
    }

    // ================== HELPER METHODS ==================

    private IMeasurable getUnitEnum(String measurementType, String unitStr) {
        switch (measurementType) {
            case "LengthUnit": return LengthUnit.valueOf(unitStr);
            case "VolumeUnit": return VolumeUnit.valueOf(unitStr);
            case "WeightUnit": return WeightUnit.valueOf(unitStr);
            case "TemperatureUnit": return TemperatureUnit.valueOf(unitStr);
            default: throw new IllegalArgumentException("Unknown measurement type: " + measurementType);
        }
    }

    private boolean performComparison(QuantityDTO q1, QuantityDTO q2) {
        if (!q1.getMeasurementType().equals(q2.getMeasurementType())) {
            throw new IllegalArgumentException("Cannot compare different measurement types");
        }
        IMeasurable u1 = getUnitEnum(q1.getMeasurementType(), q1.getUnit());
        IMeasurable u2 = getUnitEnum(q2.getMeasurementType(), q2.getUnit());
        
        double val1 = u1.convertToBaseUnit(q1.getValue());
        double val2 = u2.convertToBaseUnit(q2.getValue());
        
        return Math.abs(val1 - val2) < COMPARISON_TOLERANCE;
    }

    private double convertValue(QuantityDTO q, String targetUnitStr) {
        IMeasurable fromUnit = getUnitEnum(q.getMeasurementType(), q.getUnit());
        IMeasurable toUnit = getUnitEnum(q.getMeasurementType(), targetUnitStr);
        
        double baseValue = fromUnit.convertToBaseUnit(q.getValue());
        return toUnit.convertFromBaseUnit(baseValue);
    }

    private QuantityMeasurementDTO performArithmetic(
            QuantityDTO q1,
            QuantityDTO q2,
            QuantityDTO target,
            String operation,
            java.util.function.DoubleBinaryOperator operator) {

        QuantityMeasurementEntity entity = new QuantityMeasurementEntity();

        if (!q1.getMeasurementType().equals(q2.getMeasurementType())) {
            throw new IllegalArgumentException("Cannot perform arithmetic on different measurement types");
        }

        IMeasurable u1 = getUnitEnum(q1.getMeasurementType(), q1.getUnit());
        u1.validateOperationSupport(operation);
        IMeasurable u2 = getUnitEnum(q2.getMeasurementType(), q2.getUnit());

        double base1 = u1.convertToBaseUnit(q1.getValue());
        double base2 = u2.convertToBaseUnit(q2.getValue());

        double resultInBase = operator.applyAsDouble(base1, base2);

        String resultUnitStr = target != null ? target.getUnit() : q1.getUnit();
        IMeasurable resultUnitEnum = getUnitEnum(q1.getMeasurementType(), resultUnitStr);

        double finalResult = resultUnitEnum.convertFromBaseUnit(resultInBase);

        entity.setThisValue(q1.getValue());
        entity.setThisUnit(q1.getUnit());
        entity.setThisMeasurementType(q1.getMeasurementType());

        entity.setThatValue(q2.getValue());
        entity.setThatUnit(q2.getUnit());
        entity.setThatMeasurementType(q2.getMeasurementType());

        entity.setOperation(operation);
        entity.setResultValue(finalResult);
        entity.setResultUnit(resultUnitStr);
        entity.setResultMeasurementType(q1.getMeasurementType());
        entity.setError(false);
        entity.setUser(getCurrentUser());

        repository.save(entity);
        return QuantityMeasurementDTO.from(entity);
    }
}