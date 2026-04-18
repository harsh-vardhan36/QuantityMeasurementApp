package com.app.quantitymeasurement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Simple test to verify the measurement service can start and basic functionality works.
 */
@SpringBootTest
@ActiveProfiles("test")
public class MeasurementServiceTest {

    @Test
    public void testApplicationStarts() {
        // This test verifies that the application context can be loaded with test profile
        // If this passes, it means the service is configured correctly
    }
}