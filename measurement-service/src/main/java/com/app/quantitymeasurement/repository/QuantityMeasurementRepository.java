package com.app.quantitymeasurement.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.quantitymeasurement.entity.QuantityMeasurementEntity;

/**
 * Repository interface for QuantityMeasurementEntity.
 * Extends JpaRepository to provide standard CRUD operations and
 * custom queries for measurement history and statistics.
 *
 * Uses Long userId instead of User entity for stateless microservice communication.
 * Added deleteByUserId for the deleteAllHistory service method.
 */
public interface QuantityMeasurementRepository
        extends JpaRepository<QuantityMeasurementEntity, Long> {

    // Find all measurements by operation type
    List<QuantityMeasurementEntity> findByOperation(String operation);
    List<QuantityMeasurementEntity> findByUserIdAndOperation(Long userId, String operation);

    // Find all measurements by measurement type
    List<QuantityMeasurementEntity> findByThisMeasurementType(String measurementType);
    List<QuantityMeasurementEntity> findByUserIdAndThisMeasurementType(Long userId, String measurementType);

    // Find measurements created after a specific date
    List<QuantityMeasurementEntity> findByCreatedAtAfter(LocalDateTime date);
    List<QuantityMeasurementEntity> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime date);

    // Custom JPQL query for successful operations
    @Query("SELECT e FROM QuantityMeasurementEntity e WHERE e.operation = :operation AND e.isError = false")
    List<QuantityMeasurementEntity> findSuccessfulOperations(@Param("operation") String operation);

    @Query("SELECT e FROM QuantityMeasurementEntity e WHERE e.userId = :userId AND e.operation = :operation AND e.isError = false")
    List<QuantityMeasurementEntity> findSuccessfulOperationsByUserId(
            @Param("userId") Long userId, @Param("operation") String operation);

    // Count successful operations
    long countByOperationAndIsErrorFalse(String operation);
    long countByUserIdAndOperationAndIsErrorFalse(Long userId, String operation);

    // Find measurements with errors
    List<QuantityMeasurementEntity> findByIsErrorTrue();
    List<QuantityMeasurementEntity> findByUserIdAndIsErrorTrue(Long userId);

    // ── DELETE operations (needed for deleteAllHistory) ───────────────────────

    @Modifying
    @Query("DELETE FROM QuantityMeasurementEntity e WHERE e.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
