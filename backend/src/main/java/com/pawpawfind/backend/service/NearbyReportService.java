package com.pawpawfind.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.pawpawfind.backend.dto.NearbyReportResponse;
import com.pawpawfind.backend.entity.ReportPhotos;
import com.pawpawfind.backend.entity.Reports;
import com.pawpawfind.backend.repository.ReportPhotoRepository;
import com.pawpawfind.backend.repository.ReportRepository;
import com.pawpawfind.backend.service.GeoDistanceCalculator.BoundingBox;

/** bounding box 후보를 Haversine 거리로 정제해 주변 제보 페이지를 만든다. */
@Service
public class NearbyReportService {

	private static final double MAX_RADIUS_METERS = 20_000.0;
	private static final double BOUNDARY_EPSILON_METERS = 0.01;
	private final ReportRepository reportRepository;
	private final ReportPhotoRepository reportPhotoRepository;
	private final GeoDistanceCalculator distanceCalculator;

	public NearbyReportService(ReportRepository reportRepository,
			ReportPhotoRepository reportPhotoRepository,
			GeoDistanceCalculator distanceCalculator) {
		this.reportRepository = reportRepository;
		this.reportPhotoRepository = reportPhotoRepository;
		this.distanceCalculator = distanceCalculator;
	}

	public Page<NearbyReportResponse> search(double latitude, double longitude, double radiusMeters,
			String reportType, String species, int page, int size) {
		validate(latitude, longitude, radiusMeters, page, size);
		String normalizedReportType = normalizeReportType(reportType);
		String normalizedSpecies = normalizeOptional(species);
		BoundingBox box = distanceCalculator.boundingBox(latitude, longitude, radiusMeters);
		List<Reports> candidates = box.crossesDateLine()
				? reportRepository.findNearbyCandidatesAcrossDateLine(box.minLatitude(), box.maxLatitude(),
						box.minLongitude(), box.maxLongitude(), normalizedReportType, normalizedSpecies)
				: reportRepository.findNearbyCandidates(box.minLatitude(), box.maxLatitude(),
						box.minLongitude(), box.maxLongitude(), normalizedReportType, normalizedSpecies);

		List<LocatedReport> located = candidates.stream()
				.filter(report -> "OPEN".equals(report.getStatus()))
				.map(report -> new LocatedReport(report, distanceCalculator.distanceMeters(latitude, longitude,
						report.getLatitude(), report.getLongitude())))
				.filter(item -> item.distanceMeters() <= radiusMeters + BOUNDARY_EPSILON_METERS)
				.sorted(Comparator.comparingDouble(LocatedReport::distanceMeters)
						.thenComparing(item -> item.report().getReportId()))
				.toList();

		int fromIndex = (int) Math.min((long) page * size, located.size());
		int toIndex = Math.min(fromIndex + size, located.size());
		List<LocatedReport> pageItems = located.subList(fromIndex, toIndex);
		Map<Long, String> thumbnails = loadThumbnailUrls(pageItems);
		List<NearbyReportResponse> content = pageItems.stream()
				.map(item -> NearbyReportResponse.from(item.report(),
						thumbnails.get(item.report().getReportId()), Math.round(item.distanceMeters())))
				.toList();
		return new PageImpl<>(content, PageRequest.of(page, size), located.size());
	}

	private void validate(double latitude, double longitude, double radiusMeters, int page, int size) {
		if (!Double.isFinite(latitude) || latitude < -90.0 || latitude > 90.0) {
			throw badRequest("latitude는 -90 이상 90 이하의 유한한 숫자여야 합니다.");
		}
		if (!Double.isFinite(longitude) || longitude < -180.0 || longitude > 180.0) {
			throw badRequest("longitude는 -180 이상 180 이하의 유한한 숫자여야 합니다.");
		}
		if (!Double.isFinite(radiusMeters) || radiusMeters <= 0.0 || radiusMeters > MAX_RADIUS_METERS) {
			throw badRequest("radiusMeters는 0보다 크고 20000 이하여야 합니다.");
		}
		if (page < 0) throw badRequest("page는 0 이상이어야 합니다.");
		if (size < 1 || size > 100) throw badRequest("size는 1 이상 100 이하여야 합니다.");
	}

	private String normalizeReportType(String reportType) {
		String normalized = normalizeOptional(reportType);
		if (normalized == null) return null;
		normalized = normalized.toUpperCase(Locale.ROOT);
		if (!"LOST".equals(normalized) && !"FOUND".equals(normalized)) {
			throw badRequest("reportType은 LOST 또는 FOUND여야 합니다.");
		}
		return normalized;
	}

	private String normalizeOptional(String value) {
		if (value == null) return null;
		String normalized = value.trim();
		return normalized.isEmpty() ? null : normalized;
	}

	private ResponseStatusException badRequest(String reason) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
	}

	private Map<Long, String> loadThumbnailUrls(List<LocatedReport> reports) {
		Map<Long, String> thumbnails = new HashMap<>();
		List<Long> reportIds = reports.stream().map(item -> item.report().getReportId()).toList();
		if (reportIds.isEmpty()) return thumbnails;
		List<ReportPhotos> photos = new ArrayList<>(reportPhotoRepository.findByReportIdIn(reportIds));
		photos.sort(Comparator
				.comparing(ReportPhotos::getSortOrder, Comparator.nullsLast(Integer::compareTo))
				.thenComparing(ReportPhotos::getId, Comparator.nullsLast(Long::compareTo)));
		photos.forEach(photo -> thumbnails.putIfAbsent(photo.getReportId(), photo.getPhotoUrl()));
		return thumbnails;
	}

	private record LocatedReport(Reports report, double distanceMeters) {
	}
}
