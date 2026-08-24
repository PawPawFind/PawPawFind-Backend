package com.pawpawfind.backend.dto;

import java.util.List;

public class SearchAreaItemResponse {

	private Integer rank;
	private SearchAreaCenterResponse center;
	private Double radiusMeters;
	private Double priorityScore;
	private List<String> reasonCodes;
	private String reason;

	public Integer getRank() { return rank; }
	public void setRank(Integer rank) { this.rank = rank; }
	public SearchAreaCenterResponse getCenter() { return center; }
	public void setCenter(SearchAreaCenterResponse center) { this.center = center; }
	public Double getRadiusMeters() { return radiusMeters; }
	public void setRadiusMeters(Double radiusMeters) { this.radiusMeters = radiusMeters; }
	public Double getPriorityScore() { return priorityScore; }
	public void setPriorityScore(Double priorityScore) { this.priorityScore = priorityScore; }
	public List<String> getReasonCodes() { return reasonCodes; }
	public void setReasonCodes(List<String> reasonCodes) { this.reasonCodes = reasonCodes; }
	public String getReason() { return reason; }
	public void setReason(String reason) { this.reason = reason; }
}
