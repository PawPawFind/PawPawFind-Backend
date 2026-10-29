package com.pawpawfind.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class SearchAreaRankChangeResponse {

	private Integer rank;
	private String status;
	private SearchAreaCenterResponse beforeCenter;
	private SearchAreaCenterResponse afterCenter;
	private Double centerShiftMeters;
	private Integer beforeRadiusMeters;
	private Integer afterRadiusMeters;
	private Integer beforePriorityScore;
	private Integer afterPriorityScore;
	private Integer scoreDelta;
	private List<String> addedReasonCodes;
	private List<String> removedReasonCodes;

	public Integer getRank() { return rank; }
	public void setRank(Integer rank) { this.rank = rank; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public SearchAreaCenterResponse getBeforeCenter() { return beforeCenter; }
	public void setBeforeCenter(SearchAreaCenterResponse beforeCenter) { this.beforeCenter = beforeCenter; }
	public SearchAreaCenterResponse getAfterCenter() { return afterCenter; }
	public void setAfterCenter(SearchAreaCenterResponse afterCenter) { this.afterCenter = afterCenter; }
	public Double getCenterShiftMeters() { return centerShiftMeters; }
	public void setCenterShiftMeters(Double centerShiftMeters) { this.centerShiftMeters = centerShiftMeters; }
	public Integer getBeforeRadiusMeters() { return beforeRadiusMeters; }
	public void setBeforeRadiusMeters(Integer beforeRadiusMeters) { this.beforeRadiusMeters = beforeRadiusMeters; }
	public Integer getAfterRadiusMeters() { return afterRadiusMeters; }
	public void setAfterRadiusMeters(Integer afterRadiusMeters) { this.afterRadiusMeters = afterRadiusMeters; }
	public Integer getBeforePriorityScore() { return beforePriorityScore; }
	public void setBeforePriorityScore(Integer beforePriorityScore) { this.beforePriorityScore = beforePriorityScore; }
	public Integer getAfterPriorityScore() { return afterPriorityScore; }
	public void setAfterPriorityScore(Integer afterPriorityScore) { this.afterPriorityScore = afterPriorityScore; }
	public Integer getScoreDelta() { return scoreDelta; }
	public void setScoreDelta(Integer scoreDelta) { this.scoreDelta = scoreDelta; }
	public List<String> getAddedReasonCodes() { return addedReasonCodes; }
	public void setAddedReasonCodes(List<String> addedReasonCodes) { this.addedReasonCodes = addedReasonCodes; }
	public List<String> getRemovedReasonCodes() { return removedReasonCodes; }
	public void setRemovedReasonCodes(List<String> removedReasonCodes) { this.removedReasonCodes = removedReasonCodes; }
}
