package com.pawpawfind.backend.service;

import java.util.Objects;
import java.util.Set;

import com.pawpawfind.backend.dto.SearchAreaAlgorithmResponse;
import com.pawpawfind.backend.dto.SearchAreaCenterResponse;
import com.pawpawfind.backend.dto.SearchAreaCompareResponse;
import com.pawpawfind.backend.dto.SearchAreaComparisonResponse;
import com.pawpawfind.backend.dto.SearchAreaExplainedItemResponse;
import com.pawpawfind.backend.dto.SearchAreaRankChangeResponse;
import com.pawpawfind.backend.dto.SearchAreaScoreBreakdownResponse;

/** AI의 필수 비교 응답 계약만 검증하며 점수·거리·순위는 계산하지 않는다. */
final class SearchAreaCompareValidator {

	private SearchAreaCompareValidator() {}

	static boolean isValid(SearchAreaCompareResponse response, Long reportId) {
		if (response == null || response.getReportId() == null
				|| !Objects.equals(response.getReportId(), reportId) || response.getComparedAt() == null
				|| !validAlgorithm(response.getBaseline(), "HEURISTIC_V1")
				|| !validAlgorithm(response.getImproved(), "HEURISTIC_V2")) return false;
		SearchAreaComparisonResponse comparison = response.getComparison();
		return comparison != null && comparison.getFallbackUsed() != null && comparison.getSummary() != null
				&& comparison.getAreaCountBefore() != null && comparison.getAreaCountAfter() != null
				&& comparison.getRankChanges() != null
				&& comparison.getRankChanges().stream().allMatch(SearchAreaCompareValidator::validChange);
	}

	private static boolean validAlgorithm(SearchAreaAlgorithmResponse algorithm, String version) {
		return algorithm != null && version.equals(algorithm.getAlgorithmVersion())
				&& algorithm.getBehaviorType() != null && algorithm.getEstimatedRadiusMeters() != null
				&& algorithm.getEnvironmentSource() != null && algorithm.getFallbackUsed() != null
				&& algorithm.getAssumptions() != null && algorithm.getAreas() != null
				&& algorithm.getAreas().stream().allMatch(area -> validArea(area, algorithm.getFallbackUsed()));
	}

	private static boolean validArea(SearchAreaExplainedItemResponse area, boolean fallback) {
		if (area == null || area.getRank() == null || !validCenter(area.getCenter())
				|| area.getRadiusMeters() == null || area.getPriorityScore() == null
				|| area.getReasonCodes() == null || area.getReason() == null) return false;
		SearchAreaScoreBreakdownResponse score = area.getScoreBreakdown();
		if (score == null) return fallback;
		return inRange(score.getDistance(), 30) && inRange(score.getBehaviorEnvironment(), 30)
				&& inRange(score.getCorridor(), 20) && inRange(score.getAccessibility(), 10)
				&& inRange(score.getEventContext(), 10)
				&& score.getDistance() + score.getBehaviorEnvironment() + score.getCorridor()
						+ score.getAccessibility() + score.getEventContext() == area.getPriorityScore();
	}

	private static boolean inRange(Integer value, int maximum) {
		return value != null && value >= 0 && value <= maximum;
	}

	private static boolean validCenter(SearchAreaCenterResponse center) {
		return center != null && center.getLatitude() != null && center.getLongitude() != null;
	}

	private static boolean validChange(SearchAreaRankChangeResponse change) {
		if (change == null || change.getRank() == null || change.getStatus() == null
				|| !Set.of("BOTH", "ADDED", "REMOVED").contains(change.getStatus())
				|| change.getAddedReasonCodes() == null || change.getRemovedReasonCodes() == null) return false;
		boolean before = !"ADDED".equals(change.getStatus());
		boolean after = !"REMOVED".equals(change.getStatus());
		return (!before || (validCenter(change.getBeforeCenter()) && change.getBeforeRadiusMeters() != null
				&& change.getBeforePriorityScore() != null))
				&& (!after || (validCenter(change.getAfterCenter()) && change.getAfterRadiusMeters() != null
				&& change.getAfterPriorityScore() != null))
				&& (!(before && after) || (change.getCenterShiftMeters() != null && change.getScoreDelta() != null));
	}
}
