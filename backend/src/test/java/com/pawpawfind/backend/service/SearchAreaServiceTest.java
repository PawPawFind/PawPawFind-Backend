package com.pawpawfind.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.pawpawfind.backend.dto.SearchAreaAiRequest;
import com.pawpawfind.backend.dto.SearchAreaResponse;
import com.pawpawfind.backend.entity.Reports;
import com.pawpawfind.backend.repository.ReportFeatureRepository;

class SearchAreaServiceTest {

	private ReportService reportService;
	private ReportFeatureRepository featureRepository;
	private SearchAreaRequestMapper mapper;
	private SearchAreaAiClient aiClient;
	private SearchAreaService service;

	@BeforeEach
	void setUp() {
		reportService = mock(ReportService.class);
		featureRepository = mock(ReportFeatureRepository.class);
		mapper = mock(SearchAreaRequestMapper.class);
		aiClient = mock(SearchAreaAiClient.class);
		service = new SearchAreaService(reportService, featureRepository, mapper, aiClient);
	}

	@Test
	void ownerCanRequestLostDogRecommendationWithoutBehaviorFeatures() {
		Reports report = report("LOST", "강아지");
		SearchAreaAiRequest request = new SearchAreaAiRequest();
		SearchAreaResponse expected = new SearchAreaResponse();
		when(reportService.getReport(14L)).thenReturn(report);
		when(featureRepository.findByReportId(14L)).thenReturn(List.of());
		when(mapper.map(report, List.of())).thenReturn(request);
		when(aiClient.recommend(request)).thenReturn(expected);

		assertThat(service.recommend(14L, 7L, "USER")).isSameAs(expected);
		verify(reportService).assertCanManageReport(14L, 7L, "USER");
		verify(featureRepository).findByReportId(14L);
	}

	@Test
	void adminCanRequestRecommendation() {
		Reports report = report("LOST", "강아지");
		when(reportService.getReport(14L)).thenReturn(report);
		when(featureRepository.findByReportId(14L)).thenReturn(List.of());
		when(mapper.map(any(), any())).thenReturn(new SearchAreaAiRequest());
		when(aiClient.recommend(any())).thenReturn(new SearchAreaResponse());

		service.recommend(14L, 99L, "ADMIN");
		verify(reportService).assertCanManageReport(14L, 99L, "ADMIN");
	}

	@Test
	void propagatesNotFoundUnauthenticatedAndForbiddenPolicy() {
		assertDelegatedStatus(HttpStatus.NOT_FOUND, 7L, "USER");
		setUp();
		assertDelegatedStatus(HttpStatus.UNAUTHORIZED, null, null);
		setUp();
		assertDelegatedStatus(HttpStatus.FORBIDDEN, 8L, "USER");
	}

	@Test
	void rejectsFoundReportAndNonDogReport() {
		when(reportService.getReport(14L)).thenReturn(report("FOUND", "강아지"));
		assertStatus(() -> service.recommend(14L, 7L, "USER"), HttpStatus.UNPROCESSABLE_ENTITY);

		when(reportService.getReport(14L)).thenReturn(report("LOST", "고양이"));
		assertStatus(() -> service.recommend(14L, 7L, "USER"), HttpStatus.UNPROCESSABLE_ENTITY);
	}

	private void assertDelegatedStatus(HttpStatus status, Long userId, String role) {
		doThrow(new ResponseStatusException(status)).when(reportService)
				.assertCanManageReport(14L, userId, role);
		assertStatus(() -> service.recommend(14L, userId, role), status);
	}

	private void assertStatus(Runnable action, HttpStatus status) {
		assertThatThrownBy(action::run).isInstanceOfSatisfying(ResponseStatusException.class,
				exception -> assertThat(exception.getStatusCode()).isEqualTo(status));
	}

	private Reports report(String reportType, String species) {
		Reports report = new Reports();
		report.setReportId(14L);
		report.setReportType(reportType);
		report.setSpecies(species);
		return report;
	}
}
