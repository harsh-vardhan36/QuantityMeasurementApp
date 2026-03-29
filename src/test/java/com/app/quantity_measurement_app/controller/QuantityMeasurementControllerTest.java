package com.app.quantity_measurement_app.controller;

import com.app.quantity_measurement_app.dto.QuantityDTO;
import com.app.quantity_measurement_app.dto.QuantityInputDTO;
import com.app.quantity_measurement_app.dto.QuantityMeasurementDTO;
import com.app.quantity_measurement_app.service.IQuantityMeasurementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class QuantityMeasurementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private IQuantityMeasurementService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCompareQuantities() throws Exception {
        QuantityDTO q1 = new QuantityDTO(1.0, "FEET", "LengthUnit");
        QuantityDTO q2 = new QuantityDTO(12.0, "INCHES", "LengthUnit");
        QuantityInputDTO input = new QuantityInputDTO(q1, q2);

        QuantityMeasurementDTO result = new QuantityMeasurementDTO();
        result.setOperation("compare");
        result.setResultString("true");
        result.setError(false);

        Mockito.when(service.compareQuantities(Mockito.any(), Mockito.any())).thenReturn(result);

        mockMvc.perform(post("/api/v1/quantities/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("compare"))
                .andExpect(jsonPath("$.resultString").value("true"));
    }

    @Test
    void testAddQuantities() throws Exception {
        QuantityDTO q1 = new QuantityDTO(1.0, "FEET", "LengthUnit");
        QuantityDTO q2 = new QuantityDTO(12.0, "INCHES", "LengthUnit");
        QuantityInputDTO input = new QuantityInputDTO(q1, q2);

        QuantityMeasurementDTO result = new QuantityMeasurementDTO();
        result.setOperation("add");
        result.setResultValue(2.0);
        result.setResultUnit("FEET");

        Mockito.when(service.addQuantities(Mockito.any(), Mockito.any())).thenReturn(result);

        mockMvc.perform(post("/api/v1/quantities/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("add"))
                .andExpect(jsonPath("$.resultValue").value(2.0));
    }

    @Test
    void testConvertQuantity() throws Exception {
        QuantityDTO q1 = new QuantityDTO(1.0, "FEET", "LengthUnit");
        QuantityDTO q2 = new QuantityDTO(0.0, "INCHES", "LengthUnit");
        QuantityInputDTO input = new QuantityInputDTO(q1, q2);

        QuantityMeasurementDTO result = new QuantityMeasurementDTO();
        result.setOperation("convert");
        result.setResultValue(12.0);

        Mockito.when(service.convertQuantity(Mockito.any(), Mockito.any())).thenReturn(result);

        mockMvc.perform(post("/api/v1/quantities/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("convert"))
                .andExpect(jsonPath("$.resultValue").value(12.0));
    }

    @Test
    void testGetOperationHistory() throws Exception {
        QuantityMeasurementDTO dto = new QuantityMeasurementDTO();
        dto.setOperation("compare");

        Mockito.when(service.getHistoryByOperation("compare")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/quantities/history/operation/compare"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].operation").value("compare"));
    }

    @Test
    void testGetOperationCount() throws Exception {
        Mockito.when(service.getCountByOperation("COMPARE")).thenReturn(5L);

        mockMvc.perform(get("/api/v1/quantities/count/COMPARE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }
}