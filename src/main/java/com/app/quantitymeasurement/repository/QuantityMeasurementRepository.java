package com.app.quantitymeasurement.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.quantitymeasurement.entity.QuantityMeasurementEntity;

/**
 * Repository interface for QuantityMeasurementEntity.
 * Extends JpaRepository to provide standard CRUD operations and 
 * custom queries for measurement history and statistics.
 */
public interface QuantityMeasurementRepository 
        extends JpaRepository<QuantityMeasurementEntity, Long> {

    // Find all measurements by operation type
    List<QuantityMeasurementEntity> findByOperation(String operation);
    List<QuantityMeasurementEntity> findByUserAndOperation(com.app.quantitymeasurement.entity.User user, String operation);

    // Find all measurements by measurement type
    List<QuantityMeasurementEntity> findByThisMeasurementType(String measurementType);
    List<QuantityMeasurementEntity> findByUserAndThisMeasurementType(com.app.quantitymeasurement.entity.User user, String measurementType);

    // Find measurements created after a specific date
    List<QuantityMeasurementEntity> findByCreatedAtAfter(LocalDateTime date);
    List<QuantityMeasurementEntity> findByUserAndCreatedAtAfter(com.app.quantitymeasurement.entity.User user, LocalDateTime date);

    // Custom JPQL query for successful operations
    @Query("SELECT e FROM QuantityMeasurementEntity e WHERE e.operation = :operation AND e.isError = false")
    List<QuantityMeasurementEntity> findSuccessfulOperations(@Param("operation") String operation);

    @Query("SELECT e FROM QuantityMeasurementEntity e WHERE e.user = :user AND e.operation = :operation AND e.isError = false")
    List<QuantityMeasurementEntity> findSuccessfulOperationsByUser(@Param("user") com.app.quantitymeasurement.entity.User user, @Param("operation") String operation);

    // Count successful operations
    long countByOperationAndIsErrorFalse(String operation);
    long countByUserAndOperationAndIsErrorFalse(com.app.quantitymeasurement.entity.User user, String operation);

    // Find measurements with errors
    List<QuantityMeasurementEntity> findByIsErrorTrue();
    List<QuantityMeasurementEntity> findByUserAndIsErrorTrue(com.app.quantitymeasurement.entity.User user);
}