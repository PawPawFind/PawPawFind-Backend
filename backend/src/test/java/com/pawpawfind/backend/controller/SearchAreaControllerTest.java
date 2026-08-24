package com.pawpawfind.backend.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.pawpawfind.backend.config.JwtAuthFilter;
import com.pawpawfind.backend.dto.SearchAreaCenterResponse;
import com.pawpawfind.backend.dto.SearchAreaItemResponse;
import com.pawpawfind.backend.dto.SearchAreaResponse;
import com.pawpawfind.backend.service.SearchAreaService;

class SearchAreaControllerTest {

	private SearchAreaService service;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		service = mock(SearchAreaService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new SearchAreaController(service)).build();
	}

	@Test
	void returnsCompleteCamelCaseResponseIncludingFallback() throws Exception {
		when(service.recommend(14L, 7L, "USER")).thenReturn(response());

		mockMvc.perform(post("/api/reports/14/search-areas")
					.requestAttr(JwtAuthFilter.USER_ID_ATTR, 7L)
					.requestAttr(JwtAuthFilter.ROLE_ATTR, "USER"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reportId").value(14))
				.andExpect(jsonPath("$.algorithmVersion").value("v1"))
				.andExpect(jsonPath("$.behaviorType").value("CAUTIOUS"))
				.andExpect(jsonPath("$.fallbackUsed").value(true))
				.andExpect(jsonPath("$.assumptions[0]").value("night"))
				.andExpect(jsonPath("$.areas[0].center.latitude").value(37.5))
				.andExpect(jsonPath("$.areas[0].center.longitude").value(127.0))
				.andExpect(jsonPath("$.areas[0].radiusMeters").value(250.0))
				.andExpect(jsonPath("$.areas[0].priorityScore").value(0.8))
				.andExpect(jsonPath("$.areas[0].reasonCodes[0]").value("HOME"));
	}

	@Test
	void returnsEachPolicyStatusWithoutExposingExceptionDetails() throws Exception {
		assertStatus(HttpStatus.UNAUTHORIZED);
		assertStatus(HttpStatus.FORBIDDEN);
		assertStatus(HttpStatus.NOT_FOUND);
		assertStatus(HttpStatus.UNPROCESSABLE_ENTITY);
		assertStatus(HttpStatus.SERVICE_UNAVAILABLE);
		assertStatus(HttpStatus.BAD_GATEWAY);
	}

	private void assertStatus(HttpStatus expected) throws Exception {
		doThrow(new ResponseStatusException(expected, "internal http://secret"))
				.when(service).recommend(14L, null, null);
		mockMvc.perform(post("/api/reports/14/search-areas"))
				.andExpect(status().is(expected.value()))
				.andExpect(jsonPath("$").doesNotExist());
	}

	private SearchAreaResponse response() {
		SearchAreaCenterResponse center = new SearchAreaCenterResponse();
		center.setLatitude(37.5);
		center.setLongitude(127.0);
		SearchAreaItemResponse area = new SearchAreaItemResponse();
		area.setRank(1);
		area.setCenter(center);
		area.setRadiusMeters(250.0);
		area.setPriorityScore(0.8);
		area.setReasonCodes(List.of("HOME"));
		area.setReason("주거지");
		SearchAreaResponse response = new SearchAreaResponse();
		response.setReportId(14L);
		response.setAlgorithmVersion("v1");
		response.setBehaviorType("CAUTIOUS");
		response.setEstimatedRadiusMeters(800.0);
		response.setEnvironmentSource("fallback");
		response.setFallbackUsed(true);
		response.setAssumptions(List.of("night"));
		response.setAreas(List.of(area));
		return response;
	}
}
