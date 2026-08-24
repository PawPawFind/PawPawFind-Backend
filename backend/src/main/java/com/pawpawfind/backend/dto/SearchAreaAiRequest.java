package com.pawpawfind.backend.dto;

import java.time.LocalDate;

/** BE → AI POST /search-areas 요청 body. */
public class SearchAreaAiRequest {

	private Long reportId;
	private String species;
	private String size;
	private LocalDate eventDate;
	private Integer eventHour;
	private Double latitude;
	private Double longitude;
	private String happenPlace;
	private String description;
	private SearchAreaBehaviorProfile behaviorProfile;

	public Long getReportId() { return reportId; }
	public void setReportId(Long reportId) { this.reportId = reportId; }
	public String getSpecies() { return species; }
	public void setSpecies(String species) { this.species = species; }
	public String getSize() { return size; }
	public void setSize(String size) { this.size = size; }
	public LocalDate getEventDate() { return eventDate; }
	public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
	public Integer getEventHour() { return eventHour; }
	public void setEventHour(Integer eventHour) { this.eventHour = eventHour; }
	public Double getLatitude() { return latitude; }
	public void setLatitude(Double latitude) { this.latitude = latitude; }
	public Double getLongitude() { return longitude; }
	public void setLongitude(Double longitude) { this.longitude = longitude; }
	public String getHappenPlace() { return happenPlace; }
	public void setHappenPlace(String happenPlace) { this.happenPlace = happenPlace; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public SearchAreaBehaviorProfile getBehaviorProfile() { return behaviorProfile; }
	public void setBehaviorProfile(SearchAreaBehaviorProfile behaviorProfile) { this.behaviorProfile = behaviorProfile; }
}
