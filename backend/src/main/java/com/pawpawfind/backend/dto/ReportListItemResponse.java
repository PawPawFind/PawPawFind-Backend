package com.pawpawfind.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.pawpawfind.backend.entity.Reports;

/** 제보 목록용. 대표 사진(thumbnailUrl)을 포함한다. */
public class ReportListItemResponse {

	private Long reportId;
	private Long userId;
	private String reportType;
	private String title;
	private String species;
	private String size;
	private LocalDate eventDate;
	private Integer eventHour;
	private String happenPlace;
	private Double latitude;
	private Double longitude;
	private String description;
	private String status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	/** sortOrder 기준 첫 사진. 없으면 null */
	private String thumbnailUrl;

	public static ReportListItemResponse from(Reports report, String thumbnailUrl) {
		ReportListItemResponse item = new ReportListItemResponse();
		item.reportId = report.getReportId();
		item.userId = report.getUserId();
		item.reportType = report.getReportType();
		item.title = report.getTitle();
		item.species = report.getSpecies();
		item.size = report.getSize();
		item.eventDate = report.getEventDate();
		item.eventHour = report.getEventHour();
		item.happenPlace = report.getHappenPlace();
		item.latitude = report.getLatitude();
		item.longitude = report.getLongitude();
		item.description = report.getDescription();
		item.status = report.getStatus();
		item.createdAt = report.getCreatedAt();
		item.updatedAt = report.getUpdatedAt();
		item.thumbnailUrl = thumbnailUrl;
		return item;
	}

	public Long getReportId() {
		return reportId;
	}

	public Long getUserId() {
		return userId;
	}

	public String getReportType() {
		return reportType;
	}

	public String getTitle() {
		return title;
	}

	public String getSpecies() {
		return species;
	}

	public String getSize() {
		return size;
	}

	public LocalDate getEventDate() {
		return eventDate;
	}

	public Integer getEventHour() {
		return eventHour;
	}

	public String getHappenPlace() {
		return happenPlace;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public String getDescription() {
		return description;
	}

	public String getStatus() {
		return status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
}
