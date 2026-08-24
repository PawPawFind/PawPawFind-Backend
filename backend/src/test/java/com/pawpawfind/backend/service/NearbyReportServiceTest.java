package com.pawpawfind.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.pawpawfind.backend.dto.NearbyReportResponse;
import com.pawpawfind.backend.entity.ReportPhotos;
import com.pawpawfind.backend.entity.Reports;
import com.pawpawfind.backend.repository.ReportPhotoRepository;
import com.pawpawfind.backend.repository.ReportRepository;

class NearbyReportServiceTest {

	private static final double LATITUDE = 37.5;
	private static final double LONGITUDE = 127.0;
	private ReportRepository reportRepository;
	private ReportPhotoRepository photoRepository;
	private NearbyReportService service;

	@BeforeEach
	void setUp() {
		reportRepository = mock(ReportRepository.class);
		photoRepository = mock(ReportPhotoRepository.class);
		service = new NearbyReportService(reportRepository, photoRepository, new GeoDistanceCalculator());
		when(reportRepository.findNearbyCandidates(anyDouble(), anyDouble(), anyDouble(), anyDouble(),
				nullable(String.class), nullable(String.class))).thenReturn(List.of());
	}

	@Test
	void returnsOnlyOpenReportsInsideCircleIncludingBoundary() {
		double boundaryLongitude = LONGITUDE + Math.toDegrees(1_000.0
				/ (GeoDistanceCalculator.EARTH_RADIUS_METERS * Math.cos(Math.toRadians(LATITUDE))));
		Reports center = report(1L, "LOST", "강아지", "OPEN", LATITUDE, LONGITUDE);
		Reports boundary = report(2L, "FOUND", "고양이", "OPEN", LATITUDE, boundaryLongitude);
		Reports outsideCircle = report(3L, "LOST", "강아지", "OPEN", LATITUDE + 0.0072, LONGITUDE + 0.0091);
		Reports closed = report(4L, "LOST", "강아지", "CLOSED", LATITUDE, LONGITUDE);
		whenCandidates(center, boundary, outsideCircle, closed);

		Page<NearbyReportResponse> result = service.search(LATITUDE, LONGITUDE, 1_000,
				null, null, 0, 20);

		assertThat(result.getContent()).extracting(NearbyReportResponse::getReportId)
				.containsExactly(1L, 2L);
		assertThat(result.getContent()).extracting(NearbyReportResponse::getDistanceMeters)
				.containsExactly(0L, 1_000L);
	}

	@Test
	void passesNormalizedLostFoundAndSpeciesFiltersToBoundingQuery() {
		service.search(LATITUDE, LONGITUDE, 3_000, " lost ", " 강아지 ", 0, 20);
		verify(reportRepository).findNearbyCandidates(anyDouble(), anyDouble(), anyDouble(), anyDouble(),
				eq("LOST"), eq("강아지"));

		setUp();
		service.search(LATITUDE, LONGITUDE, 3_000, " FoUnD ", null, 0, 20);
		verify(reportRepository).findNearbyCandidates(anyDouble(), anyDouble(), anyDouble(), anyDouble(),
				eq("FOUND"), nullable(String.class));

		setUp();
		service.search(LATITUDE, LONGITUDE, 3_000, null, "고양이", 0, 20);
		verify(reportRepository).findNearbyCandidates(anyDouble(), anyDouble(), anyDouble(), anyDouble(),
				nullable(String.class), eq("고양이"));
	}

	@Test
	void noFiltersAllowLostAndFoundAndNoResultsReturnEmptyPage() {
		whenCandidates(
				report(1L, "LOST", "강아지", "OPEN", LATITUDE, LONGITUDE),
				report(2L, "FOUND", "고양이", "OPEN", LATITUDE, LONGITUDE));
		Page<NearbyReportResponse> result = service.search(LATITUDE, LONGITUDE, 3_000,
				null, null, 0, 20);
		assertThat(result.getContent()).extracting(NearbyReportResponse::getReportType)
				.containsExactly("LOST", "FOUND");

		setUp();
		Page<NearbyReportResponse> empty = service.search(LATITUDE, LONGITUDE, 3_000,
				null, null, 0, 20);
		assertThat(empty.getContent()).isEmpty();
		assertThat(empty.getTotalElements()).isZero();
	}

	@Test
	void sortsByExactDistanceThenReportIdAndPaginatesAfterFiltering() {
		whenCandidates(
				report(3L, "LOST", "강아지", "OPEN", LATITUDE, LONGITUDE + 0.002),
				report(2L, "FOUND", "강아지", "OPEN", LATITUDE, LONGITUDE),
				report(1L, "LOST", "강아지", "OPEN", LATITUDE, LONGITUDE),
				report(4L, "LOST", "강아지", "OPEN", LATITUDE, LONGITUDE + 0.001));

		Page<NearbyReportResponse> result = service.search(LATITUDE, LONGITUDE, 3_000,
				null, null, 1, 2);

		assertThat(result.getContent()).extracting(NearbyReportResponse::getReportId)
				.containsExactly(4L, 3L);
		assertThat(result.getTotalElements()).isEqualTo(4);
		assertThat(result.getTotalPages()).isEqualTo(2);
		assertThat(result.getNumber()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(2);
	}

	@Test
	void resolvesThumbnailByExistingSortOrderAndIdRuleAndAllowsMissingPhoto() {
		Reports first = report(1L, "LOST", "강아지", "OPEN", LATITUDE, LONGITUDE);
		Reports second = report(2L, "FOUND", "강아지", "OPEN", LATITUDE, LONGITUDE + 0.001);
		whenCandidates(first, second);
		when(photoRepository.findByReportIdIn(List.of(1L, 2L))).thenReturn(List.of(
				photo(20L, 1L, 2, "later.jpg"), photo(10L, 1L, 1, "first.jpg")));

		Page<NearbyReportResponse> result = service.search(LATITUDE, LONGITUDE, 3_000,
				null, null, 0, 20);

		assertThat(result.getContent().get(0).getThumbnailUrl()).isEqualTo("first.jpg");
		assertThat(result.getContent().get(1).getThumbnailUrl()).isNull();
	}

	@Test
	void usesDateLineQueryWhenBoundingBoxWraps() {
		when(reportRepository.findNearbyCandidatesAcrossDateLine(anyDouble(), anyDouble(), anyDouble(),
				anyDouble(), nullable(String.class), nullable(String.class))).thenReturn(List.of());
		service.search(0.0, 179.99, 20_000, null, null, 0, 20);
		verify(reportRepository).findNearbyCandidatesAcrossDateLine(anyDouble(), anyDouble(), anyDouble(),
				anyDouble(), nullable(String.class), nullable(String.class));
	}

	@Test
	void rejectsInvalidCoordinatesRadiusPageSizeAndReportType() {
		assertBadRequest(Double.NaN, LONGITUDE, 3_000, null, 0, 20);
		assertBadRequest(Double.POSITIVE_INFINITY, LONGITUDE, 3_000, null, 0, 20);
		assertBadRequest(91, LONGITUDE, 3_000, null, 0, 20);
		assertBadRequest(LATITUDE, -181, 3_000, null, 0, 20);
		assertBadRequest(LATITUDE, Double.NEGATIVE_INFINITY, 3_000, null, 0, 20);
		assertBadRequest(LATITUDE, LONGITUDE, 0, null, 0, 20);
		assertBadRequest(LATITUDE, LONGITUDE, -1, null, 0, 20);
		assertBadRequest(LATITUDE, LONGITUDE, 20_001, null, 0, 20);
		assertBadRequest(LATITUDE, LONGITUDE, 3_000, "OTHER", 0, 20);
		assertBadRequest(LATITUDE, LONGITUDE, 3_000, null, -1, 20);
		assertBadRequest(LATITUDE, LONGITUDE, 3_000, null, 0, 0);
		assertBadRequest(LATITUDE, LONGITUDE, 3_000, null, 0, 101);
	}

	private void assertBadRequest(double latitude, double longitude, double radius, String reportType,
			int page, int size) {
		assertThatThrownBy(() -> service.search(latitude, longitude, radius, reportType, null, page, size))
				.isInstanceOfSatisfying(ResponseStatusException.class,
						exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
	}

	private void whenCandidates(Reports... reports) {
		when(reportRepository.findNearbyCandidates(anyDouble(), anyDouble(), anyDouble(), anyDouble(),
				nullable(String.class), nullable(String.class))).thenReturn(List.of(reports));
	}

	private Reports report(Long id, String type, String species, String status,
			double latitude, double longitude) {
		Reports report = new Reports();
		report.setReportId(id);
		report.setReportType(type);
		report.setSpecies(species);
		report.setStatus(status);
		report.setLatitude(latitude);
		report.setLongitude(longitude);
		return report;
	}

	private ReportPhotos photo(Long id, Long reportId, Integer sortOrder, String url) {
		ReportPhotos photo = new ReportPhotos();
		photo.setId(id);
		photo.setReportId(reportId);
		photo.setSortOrder(sortOrder);
		photo.setPhotoUrl(url);
		return photo;
	}
}
