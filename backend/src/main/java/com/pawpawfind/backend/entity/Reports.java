package com.pawpawfind.backend.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * 사용자 실종(LOST) / 목격(FOUND) 제보. 사진·특징은 별도 테이블.
 * users 테이블 연동 전이라 userId는 null 허용.
 */
@Entity
@Table(name = "reports")
public class Reports {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "report_id")
	private Long reportId;

	/** 작성자. 회원 기능 전까지는 비워 둔다. */
	@Column(name = "user_id")
	private Long userId;

	/** LOST(실종) 또는 FOUND(목격). */
	@Column(name = "report_type", length = 10, nullable = false)
	private String reportType;

	/** FOUND만 사용. LOST는 비워도 된다. */
	@Column(name = "title", length = 255)
	private String title;

	@Column(name = "species", length = 20, nullable = false)
	private String species;

	@Column(name = "size", length = 10, nullable = false)
	private String size;

	@Column(name = "color", length = 20, nullable = false)
	private String color;

	/** 실종일 또는 목격일. */
	@Column(name = "event_date", nullable = false)
	private LocalDate eventDate;

	/** 0~23. 모르면 null. */
	@Column(name = "event_hour")
	private Integer eventHour;

	@Column(name = "happen_place", length = 255, nullable = false)
	private String happenPlace;

	@Column(name = "latitude", nullable = false)
	private Double latitude;

	@Column(name = "longitude", nullable = false)
	private Double longitude;

	@Column(name = "description", columnDefinition = "TEXT", nullable = false)
	private String description;

	/** 기본값 OPEN. CLOSED는 종료된 제보. */
	@Column(name = "status", length = 20, nullable = false)
	private String status;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	public void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
		if (this.status == null) {
			this.status = "OPEN";
		}
	}

	@PreUpdate
	public void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public Long getReportId() {
		return reportId;
	}

	public void setReportId(Long reportId) {
		this.reportId = reportId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public LocalDate getEventDate() {
		return eventDate;
	}

	public void setEventDate(LocalDate eventDate) {
		this.eventDate = eventDate;
	}

	public Integer getEventHour() {
		return eventHour;
	}

	public void setEventHour(Integer eventHour) {
		this.eventHour = eventHour;
	}

	public String getHappenPlace() {
		return happenPlace;
	}

	public void setHappenPlace(String happenPlace) {
		this.happenPlace = happenPlace;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
