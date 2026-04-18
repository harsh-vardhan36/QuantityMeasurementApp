package com.app.quantitymeasurement.client;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.app.quantitymeasurement.dto.ConversionHistoryRequest;
import com.app.quantitymeasurement.dto.ConversionHistoryResponse;

@Component
public class UserServiceClientFallback implements UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public ConversionHistoryResponse saveHistory(Long userId, ConversionHistoryRequest request) {
        log.warn("user-service unavailable — history not saved for user {}", userId);
        return new ConversionHistoryResponse();   // Return empty response, not an error
    }

    @Override
    public List<ConversionHistoryResponse> getHistory(Long userId) {
        log.warn("user-service unavailable — could not fetch history for user {}", userId);
        return Collections.emptyList();
    }
}
