package com.app.quantitymeasurement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
/**
 * Integration tests for the Quantity Measurement Application.
 * This class performs end-to-end testing of the REST API endpoints using MockMvc.
 */
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.app.quantitymeasurement.dto.QuantityDTO;
import com.app.quantitymeasurement.dto.QuantityInputDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@Import(com.app.quantitymeasurement.config.TestRedisConfig.class)
public class QuantityMeasurementApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long TEST_USER_ID = 1L;

    private RequestPostProcessor testUser() {
        return authentication(new UsernamePasswordAuthenticationToken(
                TEST_USER_ID, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @BeforeEach
    void setup() {
        // No longer need to save users to repository as it's been moved to auth-service
    }

    // ---------------- Helper Methods ----------------

    private String baseUrl() {
        return "/api/user/quantities";
    }

    private QuantityInputDTO input(
            double thisValue, String thisUnit, String thisMeasurementType,
            double thatValue, String thatUnit, String thatMeasurementType
    ) {
        QuantityInputDTO inputDTO = new QuantityInputDTO();
        inputDTO.setThisQuantityDTO(new QuantityDTO(thisValue, thisUnit, thisMeasurementType));
        inputDTO.setThatQuantityDTO(new QuantityDTO(thatValue, thatUnit, thatMeasurementType));
        return inputDTO;
    }

    private QuantityInputDTO inputWithTarget(
            double thisValue, String thisUnit, String thisMeasurementType,
            double thatValue, String thatUnit, String thatMeasurementType,
            double targetValue, String targetUnit, String targetMeasurementType
    ) {
        QuantityInputDTO inputDTO = new QuantityInputDTO();
        inputDTO.setThisQuantityDTO(new QuantityDTO(thisValue, thisUnit, thisMeasurementType));
        inputDTO.setThatQuantityDTO(new QuantityDTO(thatValue, thatUnit, thatMeasurementType));
        inputDTO.setTargetQuantityDTO(new QuantityDTO(targetValue, targetUnit, targetMeasurementType));
        return inputDTO;
    }

    // ---------------- Test Cases ----------------

    @Test
    @Order(1)
    @DisplayName("Application context loads and MockMvc is available")
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("POST /compare – 1 foot equals 12 inches → true")
    void testCompare_FootEqualsInches() throws Exception {
        QuantityInputDTO body = input(1.0, "FEET", "LengthUnit", 12.0, "INCHES", "LengthUnit");
        
        mockMvc.perform(post(baseUrl() + "/compare").with(testUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultString").value("true"));
    }

    @Test
    @Order(3)
    @DisplayName("POST /compare – 1 foot does NOT equal 1 inch → false")
    void testCompare_FootNotEqualInch() throws Exception {
        QuantityInputDTO body = input(1.0, "FEET", "LengthUnit", 1.0, "INCHES", "LengthUnit");
        
        mockMvc.perform(post(baseUrl() + "/compare").with(testUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultString").value("false"));
    }

    @Test
    @Order(4)
    @DisplayName("POST /compare – 1 gallon equals 3.785 liters → true")
    void testCompare_GallonEqualsLitres() throws Exception {
        QuantityInputDTO body = input(1.0, "GALLON", "VolumeUnit", 3.785, "LITER", "VolumeUnit");
        
        mockMvc.perform(post(baseUrl() + "/compare").with(testUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultString").value("true"));
    }

    @Test
    @Order(5)
    @DisplayName("POST /compare – 212 Fahrenheit equals 100 Celsius → true")
    void testCompare_FahrenheitEqualsCelsius() throws Exception {
        QuantityInputDTO body = input(212.0, "FAHRENHEIT", "TemperatureUnit", 100.0, "CELSIUS", "TemperatureUnit");
        
        mockMvc.perform(post(baseUrl() + "/compare").with(testUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultString").value("true"));
    }

    @Test
    @Order(6)
    @DisplayName("POST /convert — convert 100 Celsius to Fahrenheit")
    void testConvert_CelsiusToFahrenheit() throws Exception {
        QuantityInputDTO body = input(100.0, "CELSIUS", "TemperatureUnit", 0.0, "FAHRENHEIT", "TemperatureUnit");
        
        mockMvc.perform(post(baseUrl() + "/convert").with(testUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultValue").value(212.0));
    }

    @Test
    @Order(7)
    @DisplayName("POST /add — add 1 gallon and 3.785 liters = 2 gallons")
    void testAdd_GallonAndLitres() throws Exception {
        QuantityInputDTO body = input(1.0, "GALLON", "VolumeUnit", 3.785, "LITER", "VolumeUnit");
        
        mockMvc.perform(post(baseUrl() + "/add").with(testUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultValue").value(2.0));
    }

    @Test
    @Order(8)
    @DisplayName("POST /add-with-target-unit - 1 foot + 12 inches = 24 inches")
    void testAddWithTargetUnit_FootAndInchesToInches() throws Exception {
        QuantityInputDTO body = inputWithTarget(1.0, "FEET", "LengthUnit", 12.0, "INCHES", "LengthUnit", 0.0, "INCHES", "LengthUnit");
        
        mockMvc.perform(post(baseUrl() + "/add-with-target-unit").with(testUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultValue").value(24.0));
    }

    @Test
    @Order(9)
    @DisplayName("POST /subtract - 2 feet - 12 inches = 1 foot")
    void testSubtract_FeetMinusInches() throws Exception {
        QuantityInputDTO body = input(2.0, "FEET", "LengthUnit", 12.0, "INCHES", "LengthUnit");
        
        mockMvc.perform(post(baseUrl() + "/subtract").with(testUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultValue").value(1.0));
    }

    @Test
    @Order(10)
    @DisplayName("POST /subtract-with-target-unit - 2 feet - 12 inches = 12 inches")
    void testSubtractWithTargetUnit() throws Exception {
        QuantityInputDTO body = inputWithTarget(2.0, "FEET", "LengthUnit", 12.0, "INCHES", "LengthUnit", 0.0, "INCHES", "LengthUnit");
        
        mockMvc.perform(post(baseUrl() + "/subtract-with-target-unit").with(testUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultValue").value(12.0));
    }

    @Test
    @Order(11)
    @DisplayName("POST /divide - 1 yard ÷ 1 foot = 3.0")
    void testDivide_YardByFoot() throws Exception {
        QuantityInputDTO body = input(1.0, "YARDS", "LengthUnit", 1.0, "FEET", "LengthUnit");
        
        mockMvc.perform(post(baseUrl() + "/divide").with(testUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultValue").value(3.0));
    }

    @Test
    @Order(12)
    @DisplayName("GET /history/operation/CONVERT - returns list of CONVERT operations")
    void testGetHistoryByOperation_Convert() throws Exception {
        mockMvc.perform(get(baseUrl() + "/history/operation/CONVERT").with(testUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Order(13)
    @DisplayName("GET /history/type/TemperatureUnit - returns history for TemperatureUnit measurements")
    void testGetHistoryByType_Temperature() throws Exception {
        mockMvc.perform(get(baseUrl() + "/history/type/TemperatureUnit").with(testUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Order(14)
    @DisplayName("GET /count/DIVIDE - returns count of DIVIDE operations > 0")
    void testGetOperationCount_Divide() throws Exception {
        MvcResult result = mockMvc.perform(get(baseUrl() + "/count/DIVIDE").with(testUser()))
                .andExpect(status().isOk())
                .andReturn();
        
        Long count = Long.parseLong(result.getResponse().getContentAsString());
        assertThat(count).isGreaterThan(0L);
    }

    @Test
    @Order(15)
    @DisplayName("POST /divide — 1 yard ÷ 0 foot → error, GET /history/errored returns that error")
    void testDivide_YardByFeet_Error() throws Exception {
        QuantityInputDTO body = input(1.0, "YARDS", "LengthUnit", 0.0, "FEET", "LengthUnit");

        MvcResult result = mockMvc.perform(post(baseUrl() + "/divide").with(testUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(result.getResponse().getContentAsString()).contains("Divide by zero");

        mockMvc.perform(get(baseUrl() + "/history/errored").with(testUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(16)
    @DisplayName("POST /compare — validation fails: Unit must be valid for the specified measurement type")
    void testCompare_FootEqualsInches_UnitValidationFails() throws Exception {
        QuantityInputDTO body = input(1.0, "FOOT", "LengthUnit", 12.0, "INCHES", "LengthUnit");

        MvcResult result = mockMvc.perform(post(baseUrl() + "/compare").with(testUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andReturn();
                
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertThat(result.getResponse().getContentAsString()).contains("Unit must be valid for the specified measurement type");
    }

    @Test
    @Order(17)
    @DisplayName("POST /compare — validation fails: Measurement type must be one of LengthUnit, VolumeUnit, WeightUnit, TemperatureUnit")
    void testCompare_FootEqualsInches_TypeValidationFails() throws Exception {
        QuantityInputDTO body = input(1.0, "FEET", "InvalidType", 12.0, "INCHES", "LengthUnit");

        MvcResult result = mockMvc.perform(post(baseUrl() + "/compare").with(testUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertThat(result.getResponse().getContentAsString()).contains("Measurement type must be one of");
    }
}
