package com.quantitymeasurement.user_service.controller;

import com.quantitymeasurement.user_service.dto.ConversionHistoryRequest;
import com.quantitymeasurement.user_service.dto.ConversionHistoryResponse;
import com.quantitymeasurement.user_service.entity.ConversionHistory;
import com.quantitymeasurement.user_service.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for conversion history management.
 * Called by measurement-service via Feign to persist conversion records.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class HistoryController {

    private final HistoryRepository historyRepository;

    /**
     * Save a conversion history record for a user.
     * Called by measurement-service via Feign after each conversion.
     */
    @PostMapping("/{userId}/history")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversionHistoryResponse saveHistory(
            @PathVariable Long userId,
            @RequestBody ConversionHistoryRequest request) {

        log.info("Saving conversion history for user {}: {} {} → {}", 
                userId, request.getType(), request.getFromUnit(), request.getToUnit());

        ConversionHistory history = ConversionHistory.builder()
                .userId(userId)
                .type(request.getType())
                .fromUnit(request.getFromUnit())
                .toUnit(request.getToUnit())
                .inputValue(request.getInputValue())
                .outputValue(request.getOutputValue())
                .timestamp(LocalDateTime.now())
                .build();

        ConversionHistory saved = historyRepository.save(history);
        return toResponse(saved);
    }

    /**
     * Get all conversion history for a user.
     */
    @GetMapping("/{userId}/history")
    public List<ConversionHistoryResponse> getHistory(@PathVariable Long userId) {
        log.info("Fetching conversion history for user {}", userId);
        return historyRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get conversion history filtered by type for a user.
     */
    @GetMapping("/{userId}/history/{type}")
    public List<ConversionHistoryResponse> getHistoryByType(
            @PathVariable Long userId,
            @PathVariable String type) {
        log.info("Fetching {} history for user {}", type, userId);
        return historyRepository.findByUserIdAndTypeOrderByTimestampDesc(userId, type)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get conversion count for a user.
     */
    @GetMapping("/{userId}/history/count")
    public long getHistoryCount(@PathVariable Long userId) {
        return historyRepository.countByUserId(userId);
    }

    private ConversionHistoryResponse toResponse(ConversionHistory history) {
        return ConversionHistoryResponse.builder()
                .id(history.getId())
                .userId(history.getUserId())
                .type(history.getType())
                .fromUnit(history.getFromUnit())
                .toUnit(history.getToUnit())
                .inputValue(history.getInputValue())
                .outputValue(history.getOutputValue())
                .timestamp(history.getTimestamp())
                .build();
    }
}
