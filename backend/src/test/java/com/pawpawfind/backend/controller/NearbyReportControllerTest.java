package com.pawpawfind.backend.controller;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.pawpawfind.backend.dto.NearbyReportResponse;
import com.pawpawfind.backend.entity.Reports;
import com.pawpawfind.backend.repository.ReportPhotoRepository;
import com.pawpawfind.backend.repository.ReportRepository;
import com.pawpawfind.backend.service.GeoDistanceCalculator;
import com.pawpawfind.backend.service.NearbyReportService;
import com.pawpawfind.backend.service.ReportService;

class NearbyReportControllerTest {

	private NearbyReportService nearbyReportService;
	private ReportService reportService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		nearbyReportService = mock(NearbyReportService.class);
		reportService = mock(ReportService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(
				new NearbyReportController(nearbyReportService), new ReportController(reportService)).build();
	}

	@Test
	void publicRequestUsesDefaultsAndReturnsCamelCasePage() throws Exception {
		NearbyReportResponse item = NearbyReportResponse.from(report(12L), null, 324);
		when(nearbyReportService.search(37.5, 127.1, 3_000, null, null, 0, 20))
				.thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

		mockMvc.perform(get("/api/reports/nearby")
					.param("latitude", "37.5")
					.param("longitude", "127.1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].reportId").value(12))
				.andExpect(jsonPath("$.content[0].distanceMeters").value(324))
				.andExpect(jsonPath("$.content[0].thumbnailUrl").doesNotExist())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.number").value(0));
		verify(nearbyReportService).search(37.5, 127.1, 3_000, null, null, 0, 20);
	}

	@Test
	void emptyResultReturnsOkWithEmptyContent() throws Exception {
		when(nearbyReportService.search(anyDouble(), anyDouble(), anyDouble(),
				nullable(String.class), nullable(String.class), anyInt(), anyInt()))
				.thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

		mockMvc.perform(get("/api/reports/nearby")
					.param("latitude", "37.5").param("longitude", "127.1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isEmpty())
				.andExpect(jsonPath("$.totalElements").value(0));
	}

	@Test
	void missingRequiredCoordinatesReturnBadRequest() throws Exception {
		mockMvc.perform(get("/api/reports/nearby").param("longitude", "127.1"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/reports/nearby").param("latitude", "37.5"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void invalidValuesReturnBadRequestThroughApi() throws Exception {
		ReportRepository repository = mock(ReportRepository.class);
		NearbyReportService realService = new NearbyReportService(repository,
				mock(ReportPhotoRepository.class), new GeoDistanceCalculator());
		MockMvc validationMvc = MockMvcBuilders.standaloneSetup(new NearbyReportController(realService)).build();

		assertBadRequest(validationMvc, "91", "127", "3000", "0", "20", null);
		assertBadRequest(validationMvc, "NaN", "127", "3000", "0", "20", null);
		assertBadRequest(validationMvc, "37", "181", "3000", "0", "20", null);
		assertBadRequest(validationMvc, "37", "Infinity", "3000", "0", "20", null);
		assertBadRequest(validationMvc, "37", "127", "0", "0", "20", null);
		assertBadRequest(validationMvc, "37", "127", "20001", "0", "20", null);
		assertBadRequest(validationMvc, "37", "127", "3000", "-1", "20", null);
		assertBadRequest(validationMvc, "37", "127", "3000", "0", "0", null);
		assertBadRequest(validationMvc, "37", "127", "3000", "0", "101", null);
		assertBadRequest(validationMvc, "37", "127", "3000", "0", "20", "OTHER");
	}

	@Test
	void nearbyLiteralDoesNotConflictWithNumericReportDetail() throws Exception {
		Reports report = report(123L);
		when(reportService.getReport(123L)).thenReturn(report);

		mockMvc.perform(get("/api/reports/123"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reportId").value(123));
	}

	private void assertBadRequest(MockMvc mvc, String latitude, String longitude, String radius,
			String page, String size, String reportType) throws Exception {
		var request = get("/api/reports/nearby")
				.param("latitude", latitude).param("longitude", longitude)
				.param("radiusMeters", radius).param("page", page).param("size", size);
		if (reportType != null) request.param("reportType", reportType);
		mvc.perform(request).andExpect(status().isBadRequest());
	}

	private Reports report(Long id) {
		Reports report = new Reports();
		report.setReportId(id);
		report.setReportType("FOUND");
		report.setTitle("갈색 강아지를 목격했어요");
		report.setSpecies("강아지");
		report.setSize("소형");
		report.setEventDate(LocalDate.of(2026, 8, 24));
		report.setEventHour(15);
		report.setHappenPlace("서울시 송파구");
		report.setLatitude(37.5001);
		report.setLongitude(127.1001);
		report.setStatus("OPEN");
		return report;
	}
}
