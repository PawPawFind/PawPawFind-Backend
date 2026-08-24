package com.pawpawfind.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.pawpawfind.backend.entity.Reports;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReportRepositoryNearbyTest {

	@Autowired
	private ReportRepository repository;

	@Test
	void boundingQueryReturnsOnlyOpenReportsAndAppliesOptionalFilters() {
		Reports lostDog = repository.save(report("LOST", "강아지", "OPEN", 37.50, 127.10));
		Reports foundCat = repository.save(report("FOUND", "고양이", "OPEN", 37.51, 127.11));
		repository.save(report("LOST", "강아지", "CLOSED", 37.50, 127.10));
		repository.save(report("LOST", "강아지", "OPEN", 38.10, 128.10));

		assertThat(query(null, null)).extracting(Reports::getReportId)
				.containsExactlyInAnyOrder(lostDog.getReportId(), foundCat.getReportId());
		assertThat(query("LOST", null)).extracting(Reports::getReportId)
				.containsExactly(lostDog.getReportId());
		assertThat(query("FOUND", null)).extracting(Reports::getReportId)
				.containsExactly(foundCat.getReportId());
		assertThat(query(null, "강아지")).extracting(Reports::getReportId)
				.containsExactly(lostDog.getReportId());
		assertThat(query("FOUND", "고양이")).extracting(Reports::getReportId)
				.containsExactly(foundCat.getReportId());
	}

	@Test
	void dateLineQueryIncludesBothLongitudeEdges() {
		Reports east = repository.save(report("LOST", "강아지", "OPEN", 0.0, 179.95));
		Reports west = repository.save(report("FOUND", "강아지", "OPEN", 0.0, -179.95));
		repository.save(report("FOUND", "강아지", "OPEN", 0.0, 170.0));

		List<Reports> results = repository.findNearbyCandidatesAcrossDateLine(
				-1.0, 1.0, 179.8, -179.8, null, null);

		assertThat(results).extracting(Reports::getReportId)
				.containsExactlyInAnyOrder(east.getReportId(), west.getReportId());
	}

	private List<Reports> query(String reportType, String species) {
		return repository.findNearbyCandidates(37.0, 38.0, 127.0, 128.0, reportType, species);
	}

	private Reports report(String type, String species, String status, double latitude, double longitude) {
		Reports report = new Reports();
		report.setReportType(type);
		report.setSpecies(species);
		report.setSize("소형");
		report.setEventDate(LocalDate.of(2026, 8, 25));
		report.setHappenPlace("테스트 장소");
		report.setLatitude(latitude);
		report.setLongitude(longitude);
		report.setStatus(status);
		return report;
	}
}
