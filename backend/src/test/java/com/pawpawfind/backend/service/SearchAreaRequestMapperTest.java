package com.pawpawfind.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.pawpawfind.backend.dto.SearchAreaAiRequest;
import com.pawpawfind.backend.dto.SearchAreaBehaviorProfile;
import com.pawpawfind.backend.entity.ReportFeatures;
import com.pawpawfind.backend.entity.Reports;

class SearchAreaRequestMapperTest {

	private final SearchAreaRequestMapper mapper = new SearchAreaRequestMapper();

	@Test
	void mapsEveryReportFieldAndAllowsNullEventHour() {
		Reports report = report();
		report.setEventHour(null);
		SearchAreaAiRequest request = mapper.map(report, List.of());

		assertThat(request.getReportId()).isEqualTo(14L);
		assertThat(request.getSpecies()).isEqualTo("강아지");
		assertThat(request.getSize()).isEqualTo("SMALL");
		assertThat(request.getEventDate()).isEqualTo(LocalDate.of(2026, 8, 24));
		assertThat(request.getEventHour()).isNull();
		assertThat(request.getLatitude()).isEqualTo(37.5665);
		assertThat(request.getLongitude()).isEqualTo(126.9780);
		assertThat(request.getHappenPlace()).isEqualTo("서울광장");
		assertThat(request.getDescription()).isEqualTo("빨간 목줄");
	}

	@Test
	void mapsSixBehaviorCategoriesWithWhitespaceAndCaseNormalization() {
		SearchAreaBehaviorProfile behavior = mapper.map(report(), List.of(
				feature(1L, " 활동량 ", " high "),
				feature(2L, "낯선사람반응", " approach "),
				feature(3L, "소음민감도", " MeDiuM "),
				feature(4L, "추격성향", "low"),
				feature(5L, "이동성", " limited "),
				feature(6L, "도주원인", " door_open "))).getBehaviorProfile();

		assertThat(behavior.getActivityLevel()).isEqualTo("HIGH");
		assertThat(behavior.getStrangerResponse()).isEqualTo("APPROACH");
		assertThat(behavior.getNoiseSensitivity()).isEqualTo("MEDIUM");
		assertThat(behavior.getChaseTendency()).isEqualTo("LOW");
		assertThat(behavior.getMobility()).isEqualTo("LIMITED");
		assertThat(behavior.getEscapeCause()).isEqualTo("DOOR_OPEN");
	}

	@Test
	void defaultsMissingAndInvalidValuesToUnknown() {
		SearchAreaBehaviorProfile empty = mapper.map(report(), List.of()).getBehaviorProfile();
		assertAllUnknown(empty);

		SearchAreaBehaviorProfile invalid = mapper.map(report(), List.of(
				feature(1L, "활동량", "VERY_HIGH"),
				feature(2L, "낯선사람반응", null))).getBehaviorProfile();
		assertAllUnknown(invalid);
	}

	@Test
	void usesHighestIdAndIgnoresUnknownAppearanceCategories() {
		SearchAreaBehaviorProfile behavior = mapper.map(report(), List.of(
				feature(5L, "활동량", "LOW"),
				feature(12L, "활동량", "HIGH"),
				feature(20L, "털색", "WHITE"),
				feature(21L, "외형", "COLLAR"),
				feature(22L, "알수없음", "HIGH"))).getBehaviorProfile();

		assertThat(behavior.getActivityLevel()).isEqualTo("HIGH");
		assertThat(behavior.getStrangerResponse()).isEqualTo("UNKNOWN");
	}

	private void assertAllUnknown(SearchAreaBehaviorProfile behavior) {
		assertThat(List.of(behavior.getActivityLevel(), behavior.getStrangerResponse(),
				behavior.getNoiseSensitivity(), behavior.getChaseTendency(), behavior.getMobility(),
				behavior.getEscapeCause())).containsOnly("UNKNOWN");
	}

	private Reports report() {
		Reports report = new Reports();
		report.setReportId(14L);
		report.setSpecies("강아지");
		report.setSize("SMALL");
		report.setEventDate(LocalDate.of(2026, 8, 24));
		report.setEventHour(21);
		report.setLatitude(37.5665);
		report.setLongitude(126.9780);
		report.setHappenPlace("서울광장");
		report.setDescription("빨간 목줄");
		return report;
	}

	private ReportFeatures feature(Long id, String category, String keyword) {
		ReportFeatures feature = new ReportFeatures();
		feature.setId(id);
		feature.setCategory(category);
		feature.setKeyword(keyword);
		return feature;
	}
}
