package com.pawpawfind.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.pawpawfind.backend.dto.SearchAreaAiRequest;
import com.pawpawfind.backend.dto.SearchAreaCompareResponse;
import com.pawpawfind.backend.entity.Reports;
import com.pawpawfind.backend.repository.ReportFeatureRepository;

class SearchAreaCompareServiceTest {

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
		SearchAreaCompareResponse expected = new SearchAreaCompareResponse();
		when(reportService.getReport(14L)).thenReturn(report);
		when(featureRepository.findByReportId(14L)).thenReturn(List.of());
		when(mapper.map(report, List.of())).thenReturn(request);
		when(aiClient.compare(request)).thenReturn(expected);

		assertThat(service.compare(14L, 7L, "USER")).isSameAs(expected);
		verify(reportService).assertCanManageReport(14L, 7L, "USER");
		verify(featureRepository).findByReportId(14L);
		var order = org.mockito.Mockito.inOrder(reportService, featureRepository, mapper, aiClient);
		order.verify(reportService).assertCanManageReport(14L, 7L, "USER");
		order.verify(reportService).getReport(14L);
		order.verify(featureRepository).findByReportId(14L);
		order.verify(mapper).map(report, List.of());
		order.verify(aiClient).compare(request);
		verifyNoMoreInteractions(aiClient);
	}

	@Test
	void adminCanRequestRecommendation() {
		Reports report = report("LOST", "강아지");
		when(reportService.getReport(14L)).thenReturn(report);
		when(featureRepository.findByReportId(14L)).thenReturn(List.of());
		when(mapper.map(any(), any())).thenReturn(new SearchAreaAiRequest());
		when(aiClient.compare(any())).thenReturn(new SearchAreaCompareResponse());

		service.compare(14L, 99L, "ADMIN");
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
		assertStatus(() -> service.compare(14L, 7L, "USER"), HttpStatus.UNPROCESSABLE_ENTITY);
		verifyNoInteractions(aiClient);

		when(reportService.getReport(14L)).thenReturn(report("LOST", "고양이"));
		assertStatus(() -> service.compare(14L, 7L, "USER"), HttpStatus.UNPROCESSABLE_ENTITY);
		verifyNoInteractions(aiClient);
	}

	@Test
	void rejectsMissingReportWithoutAiCall() {
		assertStatus(() -> service.compare(14L, 7L, "USER"), HttpStatus.NOT_FOUND);
		verifyNoInteractions(aiClient);
	}

	@Test
	void comparisonUsesExistingMapperForLatestBehaviorAndKoreanSizes() {
		service = new SearchAreaService(reportService, featureRepository, new SearchAreaRequestMapper(), aiClient);
		var older = new com.pawpawfind.backend.entity.ReportFeatures();
		older.setId(1L); older.setCategory("활동량"); older.setKeyword("LOW");
		var latest = new com.pawpawfind.backend.entity.ReportFeatures();
		latest.setId(2L); latest.setCategory("활동량"); latest.setKeyword("HIGH");
		var invalid = new com.pawpawfind.backend.entity.ReportFeatures();
		invalid.setId(3L); invalid.setCategory("소음민감도"); invalid.setKeyword("INVALID");
		when(featureRepository.findByReportId(14L)).thenReturn(List.of(older, latest, invalid));
		String[] sizes = {"SMALL", "MEDIUM", "LARGE"};
		for (String size : sizes) {
			Reports report = report("LOST", "강아지");
			report.setSize(size);
			when(reportService.getReport(14L)).thenReturn(report);
			service.compare(14L, 7L, "USER");
		}
		var captor = org.mockito.ArgumentCaptor.forClass(SearchAreaAiRequest.class);
		verify(aiClient, org.mockito.Mockito.times(3)).compare(captor.capture());
		assertThat(captor.getAllValues()).extracting(SearchAreaAiRequest::getSize)
				.containsExactly("소형", "중형", "대형");
		for (var request : captor.getAllValues()) {
			assertThat(request.getBehaviorProfile().getActivityLevel()).isEqualTo("HIGH");
			assertThat(request.getBehaviorProfile().getNoiseSensitivity()).isEqualTo("UNKNOWN");
			assertThat(request.getBehaviorProfile().getMobility()).isEqualTo("UNKNOWN");
		}
		verifyNoMoreInteractions(aiClient);
	}

	private void assertDelegatedStatus(HttpStatus status, Long userId, String role) {
		doThrow(new ResponseStatusException(status)).when(reportService)
				.assertCanManageReport(14L, userId, role);
		assertStatus(() -> service.compare(14L, userId, role), status);
		verifyNoInteractions(aiClient, mapper, featureRepository);
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
