package com.pawpawfind.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class SearchAreaComparisonResponse {

	private Boolean fallbackUsed;
	private String summary;
	private Integer areaCountBefore;
	private Integer areaCountAfter;
	private List<SearchAreaRankChangeResponse> rankChanges;

	public Boolean getFallbackUsed() { return fallbackUsed; }
	public void setFallbackUsed(Boolean fallbackUsed) { this.fallbackUsed = fallbackUsed; }
	public String getSummary() { return summary; }
	public void setSummary(String summary) { this.summary = summary; }
	public Integer getAreaCountBefore() { return areaCountBefore; }
	public void setAreaCountBefore(Integer areaCountBefore) { this.areaCountBefore = areaCountBefore; }
	public Integer getAreaCountAfter() { return areaCountAfter; }
	public void setAreaCountAfter(Integer areaCountAfter) { this.areaCountAfter = areaCountAfter; }
	public List<SearchAreaRankChangeResponse> getRankChanges() { return rankChanges; }
	public void setRankChanges(List<SearchAreaRankChangeResponse> rankChanges) { this.rankChanges = rankChanges; }
}
