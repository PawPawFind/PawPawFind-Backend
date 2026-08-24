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
		request.setSize(report.getSize());
		request.setEventDate(report.getEventDate());
		request.setEventHour(report.getEventHour());
		request.setLatitude(report.getLatitude());
		request.setLongitude(report.getLongitude());
		request.setHappenPlace(report.getHappenPlace());
		request.setDescription(report.getDescription());
		request.setBehaviorProfile(behavior);
		return request;
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
