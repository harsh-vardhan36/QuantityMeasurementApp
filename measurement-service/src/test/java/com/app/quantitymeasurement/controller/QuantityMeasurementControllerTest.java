package com.app.quantitymeasurement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.app.quantitymeasurement.dto.QuantityDTO;
import com.app.quantitymeasurement.dto.QuantityInputDTO;
import com.app.quantitymeasurement.dto.QuantityMeasurementDTO;
import com.app.quantitymeasurement.service.IQuantityMeasurementService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for the QuantityMeasurementController.
 * This class uses MockMvc to test the controller layer in isolation,
 * mocking the service dependency.
 */
@WebMvcTest(QuantityMeasurementController.class)
@AutoConfigureMockMvc(addFilters = false)
public class QuantityMeasurementControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private IQuantityMeasurementService service;

	@Autowired
	private ObjectMapper objectMapper;

	private QuantityInputDTO validCompareInput;
	private QuantityMeasurementDTO mockCompareResult;

	@BeforeEach
	void setUp() {
		// Setup input for compare and add actions
		validCompareInput = new QuantityInputDTO();
		QuantityDTO q1 = new QuantityDTO();
		q1.setValue(1.0);
		q1.setUnit("FEET");
		q1.setMeasurementType("LengthUnit");

		QuantityDTO q2 = new QuantityDTO();
		q2.setValue(12.0);
		q2.setUnit("INCHES");
		q2.setMeasurementType("LengthUnit");

		validCompareInput.setThisQuantityDTO(q1);
		validCompareInput.setThatQuantityDTO(q2);

		// Setup mock response for compare
		mockCompareResult = new QuantityMeasurementDTO();
		mockCompareResult.setThisValue(1.0);
		mockCompareResult.setThisUnit("FEET");
		mockCompareResult.setThisMeasurementType("LengthUnit");
		mockCompareResult.setThatValue(12.0);
		mockCompareResult.setThatUnit("INCHES");
		mockCompareResult.setThatMeasurementType("LengthUnit");
		mockCompareResult.setOperation("COMPARE");
		mockCompareResult.setResultString("Equal");
		mockCompareResult.setError(false);
	}

	@Test
	void testCompareQuantities_Success() throws Exception {
		Mockito.when(service.compare(any(QuantityDTO.class), any(QuantityDTO.class))).thenReturn(mockCompareResult);

		mockMvc.perform(post("/api/user/quantities/compare").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validCompareInput))).andExpect(status().isOk())
				.andExpect(jsonPath("$.operation").value("COMPARE"))
				.andExpect(jsonPath("$.resultString").value("Equal")).andExpect(jsonPath("$.error").value(false));

		Mockito.verify(service, Mockito.times(1)).compare(any(QuantityDTO.class), any(QuantityDTO.class));
	}

	@Test
	void testCompareQuantities_Error() throws Exception {
		Mockito.when(service.compare(any(), any())).thenThrow(new RuntimeException("Comparison failed"));

		mockMvc.perform(post("/api/user/quantities/compare").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validCompareInput))).andExpect(status().isBadRequest());
	}

	@Test
	void testAddQuantities_Success() throws Exception {
		QuantityMeasurementDTO mockAddResult = new QuantityMeasurementDTO();
		mockAddResult.setOperation("ADD");
		mockAddResult.setResultValue(24.0);
		mockAddResult.setResultUnit("INCHES");
		mockAddResult.setError(false);

		Mockito.when(service.add(any(QuantityDTO.class), any(QuantityDTO.class))).thenReturn(mockAddResult);

		mockMvc.perform(post("/api/user/quantities/add").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validCompareInput))).andExpect(status().isOk())
				.andExpect(jsonPath("$.operation").value("ADD")).andExpect(jsonPath("$.resultValue").value(24.0))
				.andExpect(jsonPath("$.resultUnit").value("INCHES")).andExpect(jsonPath("$.error").value(false));

		Mockito.verify(service, Mockito.times(1)).add(any(QuantityDTO.class), any(QuantityDTO.class));
	}

	@Test
	void testAddQuantities_Error() throws Exception {
		Mockito.when(service.add(any(), any())).thenThrow(new RuntimeException("Addition failed"));

		mockMvc.perform(post("/api/user/quantities/add").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validCompareInput))).andExpect(status().isBadRequest());
	}

	@Test
	void testGetOperationHistory_Success() throws Exception {
		List<QuantityMeasurementDTO> history = Arrays.asList(mockCompareResult);
		Mockito.when(service.getOperationHistory("COMPARE")).thenReturn(history);

		mockMvc.perform(get("/api/user/quantities/history/operation/COMPARE")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].operation").value("COMPARE"));

		Mockito.verify(service, Mockito.times(1)).getOperationHistory("COMPARE");
	}

	@Test
	void testGetOperationCount_Success() throws Exception {
		Mockito.when(service.getOperationCount("ADD")).thenReturn(5L);

		mockMvc.perform(get("/api/user/quantities/count/ADD")).andExpect(status().isOk())
				.andExpect(jsonPath("$").value(5));

		Mockito.verify(service, Mockito.times(1)).getOperationCount("ADD");
	}
}
