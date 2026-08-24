package com.pawpawfind.backend.dto;

import java.time.LocalDate;

import com.pawpawfind.backend.entity.Reports;

/** 지도 마커용 주변 제보 응답. */
public class NearbyReportResponse {

	private Long reportId;
	private String reportType;
	private String title;
	private String species;
	private String size;
	private LocalDate eventDate;
	private Integer eventHour;
	private String happenPlace;
	private Double latitude;
	private Double longitude;
	private String status;
	private String thumbnailUrl;
	private Long distanceMeters;

	public static NearbyReportResponse from(Reports report, String thumbnailUrl, long distanceMeters) {
		NearbyReportResponse response = new NearbyReportResponse();
		response.reportId = report.getReportId();
		response.reportType = report.getReportType();
		response.title = report.getTitle();
		response.species = report.getSpecies();
		response.size = report.getSize();
		response.eventDate = report.getEventDate();
		response.eventHour = report.getEventHour();
		response.happenPlace = report.getHappenPlace();
		response.latitude = report.getLatitude();
		response.longitude = report.getLongitude();
		response.status = report.getStatus();
		response.thumbnailUrl = thumbnailUrl;
		response.distanceMeters = distanceMeters;
		return response;
	}

	public Long getReportId() { return reportId; }
	public String getReportType() { return reportType; }
	public String getTitle() { return title; }
	public String getSpecies() { return species; }
	public String getSize() { return size; }
	public LocalDate getEventDate() { return eventDate; }
	public Integer getEventHour() { return eventHour; }
	public String getHappenPlace() { return happenPlace; }
	public Double getLatitude() { return latitude; }
	public Double getLongitude() { return longitude; }
	public String getStatus() { return status; }
	public String getThumbnailUrl() { return thumbnailUrl; }
	public Long getDistanceMeters() { return distanceMeters; }
}
