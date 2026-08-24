package com.pawpawfind.backend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawpawfind.backend.entity.Reports;

class NearbyReportResponseTest {

	@Test
	void exposesCamelCaseMapFieldsWithoutPersonalInformation() throws Exception {
		Reports report = new Reports();
		report.setReportId(12L);
		report.setUserId(99L);
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

		String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(
				NearbyReportResponse.from(report, "https://example.com/photo.jpg", 324));

		assertThat(json).contains("\"thumbnailUrl\":\"https://example.com/photo.jpg\"",
				"\"distanceMeters\":324", "\"eventDate\"");
		assertThat(json).doesNotContain("userId", "description", "createdAt", "updatedAt",
				"distance_meters");
	}
}
