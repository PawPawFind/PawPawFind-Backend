package com.pawpawfind.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class SearchAreaScoreBreakdownResponse {

	private Integer distance;
	private Integer behaviorEnvironment;
	private Integer corridor;
	private Integer accessibility;
	private Integer eventContext;

	public Integer getDistance() { return distance; }
	public void setDistance(Integer distance) { this.distance = distance; }
	public Integer getBehaviorEnvironment() { return behaviorEnvironment; }
	public void setBehaviorEnvironment(Integer behaviorEnvironment) { this.behaviorEnvironment = behaviorEnvironment; }
	public Integer getCorridor() { return corridor; }
	public void setCorridor(Integer corridor) { this.corridor = corridor; }
	public Integer getAccessibility() { return accessibility; }
	public void setAccessibility(Integer accessibility) { this.accessibility = accessibility; }
	public Integer getEventContext() { return eventContext; }
	public void setEventContext(Integer eventContext) { this.eventContext = eventContext; }
}
