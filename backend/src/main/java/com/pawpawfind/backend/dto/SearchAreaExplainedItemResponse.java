package com.pawpawfind.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class SearchAreaExplainedItemResponse {

	private Integer rank;
	private SearchAreaCenterResponse center;
	private Integer radiusMeters;
	private Integer priorityScore;
	private SearchAreaScoreBreakdownResponse scoreBreakdown;
	private List<String> reasonCodes;
	private String reason;

	public Integer getRank() { return rank; }
	public void setRank(Integer rank) { this.rank = rank; }
	public SearchAreaCenterResponse getCenter() { return center; }
	public void setCenter(SearchAreaCenterResponse center) { this.center = center; }
	public Integer getRadiusMeters() { return radiusMeters; }
	public void setRadiusMeters(Integer radiusMeters) { this.radiusMeters = radiusMeters; }
	public Integer getPriorityScore() { return priorityScore; }
	public void setPriorityScore(Integer priorityScore) { this.priorityScore = priorityScore; }
	public SearchAreaScoreBreakdownResponse getScoreBreakdown() { return scoreBreakdown; }
	public void setScoreBreakdown(SearchAreaScoreBreakdownResponse scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }
	public List<String> getReasonCodes() { return reasonCodes; }
	public void setReasonCodes(List<String> reasonCodes) { this.reasonCodes = reasonCodes; }
	public String getReason() { return reason; }
	public void setReason(String reason) { this.reason = reason; }
}
