package com.pawpawfind.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pawpawfind.backend.dto.NearbyReportResponse;
import com.pawpawfind.backend.service.NearbyReportService;

@RestController
public class NearbyReportController {

	private final NearbyReportService nearbyReportService;

	public NearbyReportController(NearbyReportService nearbyReportService) {
		this.nearbyReportService = nearbyReportService;
	}

	@GetMapping("/api/reports/nearby")
	public ResponseEntity<Page<NearbyReportResponse>> getNearbyReports(
			@RequestParam double latitude,
			@RequestParam double longitude,
			@RequestParam(defaultValue = "3000") double radiusMeters,
			@RequestParam(required = false) String reportType,
			@RequestParam(required = false) String species,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return ResponseEntity.ok(nearbyReportService.search(latitude, longitude, radiusMeters,
				reportType, species, page, size));
	}
}
