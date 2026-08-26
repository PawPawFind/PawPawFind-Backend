package com.pawpawfind.backend.service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.pawpawfind.backend.dto.SearchAreaAiRequest;
import com.pawpawfind.backend.dto.SearchAreaBehaviorProfile;
import com.pawpawfind.backend.entity.ReportFeatures;
import com.pawpawfind.backend.entity.Reports;

/** 신고와 행동 특징을 AI 수색 영역 요청 계약으로 변환한다. */
@Component
public class SearchAreaRequestMapper {

	private static final String UNKNOWN = "UNKNOWN";
	private static final Map<String, String> SIZE_CODE_TO_AI = Map.of(
			"SMALL", "소형",
			"MEDIUM", "중형",
			"LARGE", "대형");
	private static final Map<String, Set<String>> ALLOWED_VALUES = Map.of(
			"활동량", Set.of("LOW", "MEDIUM", "HIGH", UNKNOWN),
			"낯선사람반응", Set.of("APPROACH", "NEUTRAL", "AVOID", UNKNOWN),
			"소음민감도", Set.of("LOW", "MEDIUM", "HIGH", UNKNOWN),
			"추격성향", Set.of("LOW", "MEDIUM", "HIGH", UNKNOWN),
			"이동성", Set.of("NORMAL", "LIMITED", UNKNOWN),
			"도주원인", Set.of("DOOR_OPEN", "NOISE", "CHASE", UNKNOWN));

	public SearchAreaAiRequest map(Reports report, List<ReportFeatures> features) {
		Map<String, ReportFeatures> latest = latestBehaviorFeatures(features);
		SearchAreaBehaviorProfile behavior = new SearchAreaBehaviorProfile();
		behavior.setActivityLevel(value(latest, "활동량"));
		behavior.setStrangerResponse(value(latest, "낯선사람반응"));
		behavior.setNoiseSensitivity(value(latest, "소음민감도"));
		behavior.setChaseTendency(value(latest, "추격성향"));
		behavior.setMobility(value(latest, "이동성"));
		behavior.setEscapeCause(value(latest, "도주원인"));

		SearchAreaAiRequest request = new SearchAreaAiRequest();
		request.setReportId(report.getReportId());
		request.setSpecies(report.getSpecies());
		request.setSize(normalizeSize(report.getSize()));
		request.setEventDate(report.getEventDate());
		request.setEventHour(report.getEventHour());
		request.setLatitude(report.getLatitude());
		request.setLongitude(report.getLongitude());
		request.setHappenPlace(report.getHappenPlace());
		request.setDescription(report.getDescription());
		request.setBehaviorProfile(behavior);
		return request;
	}

	/**
	 * BE/DB의 크기 코드({@code SMALL}, {@code MEDIUM}, {@code LARGE})를 AI 계약이 요구하는
	 * 한글 크기({@code 소형}, {@code 중형}, {@code 대형})로 변환한다.
	 * 이미 한글 크기라면 그대로 유지하고, 매핑되지 않는 값은 기존 AI 오류 처리 흐름이 그대로
	 * 처리하도록 원본 값을 그대로 전달한다.
	 */
	private String normalizeSize(String size) {
		if (size == null) {
			return null;
		}
		String trimmed = size.trim();
		String normalized = SIZE_CODE_TO_AI.get(trimmed.toUpperCase(Locale.ROOT));
		return normalized != null ? normalized : trimmed;
	}

	private Map<String, ReportFeatures> latestBehaviorFeatures(List<ReportFeatures> features) {
		Map<String, ReportFeatures> latest = new HashMap<>();
		if (features == null) {
			return latest;
		}
		for (ReportFeatures feature : features) {
			if (feature == null || feature.getCategory() == null) {
				continue;
			}
			String category = feature.getCategory().trim();
			if (!ALLOWED_VALUES.containsKey(category)) {
				continue;
			}
			ReportFeatures current = latest.get(category);
			if (current == null || compareId(feature.getId(), current.getId()) > 0) {
				latest.put(category, feature);
			}
		}
		return latest;
	}

	private int compareId(Long left, Long right) {
		if (left == null) return right == null ? 0 : -1;
		if (right == null) return 1;
		return left.compareTo(right);
	}

	private String value(Map<String, ReportFeatures> latest, String category) {
		ReportFeatures feature = latest.get(category);
		if (feature == null || feature.getKeyword() == null) {
			return UNKNOWN;
		}
		String normalized = feature.getKeyword().trim().toUpperCase(Locale.ROOT);
		return ALLOWED_VALUES.get(category).contains(normalized) ? normalized : UNKNOWN;
	}
}
