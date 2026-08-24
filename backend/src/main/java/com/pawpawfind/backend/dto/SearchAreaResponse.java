package com.pawpawfind.backend.dto;

import java.util.List;

/** AI /search-areas 응답이자 FE 전달 계약. */
public class SearchAreaResponse {

	private Long reportId;
	private String algorithmVersion;
	private String behaviorType;
	private Double estimatedRadiusMeters;
	private String environmentSource;
	private Boolean fallbackUsed;
	private List<String> assumptions;
	private List<SearchAreaItemResponse> areas;

	public Long getReportId() { return reportId; }
	public void setReportId(Long reportId) { this.reportId = reportId; }
	public String getAlgorithmVersion() { return algorithmVersion; }
	public void setAlgorithmVersion(String algorithmVersion) { this.algorithmVersion = algorithmVersion; }
	public String getBehaviorType() { return behaviorType; }
	public void setBehaviorType(String behaviorType) { this.behaviorType = behaviorType; }
	public Double getEstimatedRadiusMeters() { return estimatedRadiusMeters; }
	public void setEstimatedRadiusMeters(Double estimatedRadiusMeters) { this.estimatedRadiusMeters = estimatedRadiusMeters; }
	public String getEnvironmentSource() { return environmentSource; }
	public void setEnvironmentSource(String environmentSource) { this.environmentSource = environmentSource; }
	public Boolean getFallbackUsed() { return fallbackUsed; }
	public void setFallbackUsed(Boolean fallbackUsed) { this.fallbackUsed = fallbackUsed; }
	public List<String> getAssumptions() { return assumptions; }
	public void setAssumptions(List<String> assumptions) { this.assumptions = assumptions; }
	public List<SearchAreaItemResponse> getAreas() { return areas; }
	public void setAreas(List<SearchAreaItemResponse> areas) { this.areas = areas; }
}
