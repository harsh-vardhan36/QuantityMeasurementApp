package com.quantitymeasurement.user_service.repository;

import com.quantitymeasurement.user_service.entity.ConversionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for ConversionHistory entity.
 */
@Repository
public interface HistoryRepository extends JpaRepository<ConversionHistory, Long> {

    /** Find all conversion history records for a specific user, ordered by most recent first. */
    List<ConversionHistory> findByUserIdOrderByTimestampDesc(Long userId);

    /** Find conversion history by user and type (e.g., LENGTH, WEIGHT). */
    List<ConversionHistory> findByUserIdAndTypeOrderByTimestampDesc(Long userId, String type);

    /** Count total conversions for a user. */
    long countByUserId(Long userId);
}
