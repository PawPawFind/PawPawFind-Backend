package com.pawpawfind.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class SearchAreaAlgorithmResponse {

	private String algorithmVersion;
	private String behaviorType;
	private Integer estimatedRadiusMeters;
	private String environmentSource;
	private Boolean fallbackUsed;
	private List<String> assumptions;
	private List<SearchAreaExplainedItemResponse> areas;

	public String getAlgorithmVersion() { return algorithmVersion; }
	public void setAlgorithmVersion(String algorithmVersion) { this.algorithmVersion = algorithmVersion; }
	public String getBehaviorType() { return behaviorType; }
	public void setBehaviorType(String behaviorType) { this.behaviorType = behaviorType; }
	public Integer getEstimatedRadiusMeters() { return estimatedRadiusMeters; }
	public void setEstimatedRadiusMeters(Integer estimatedRadiusMeters) { this.estimatedRadiusMeters = estimatedRadiusMeters; }
	public String getEnvironmentSource() { return environmentSource; }
	public void setEnvironmentSource(String environmentSource) { this.environmentSource = environmentSource; }
	public Boolean getFallbackUsed() { return fallbackUsed; }
	public void setFallbackUsed(Boolean fallbackUsed) { this.fallbackUsed = fallbackUsed; }
	public List<String> getAssumptions() { return assumptions; }
	public void setAssumptions(List<String> assumptions) { this.assumptions = assumptions; }
	public List<SearchAreaExplainedItemResponse> getAreas() { return areas; }
	public void setAreas(List<SearchAreaExplainedItemResponse> areas) { this.areas = areas; }
}
