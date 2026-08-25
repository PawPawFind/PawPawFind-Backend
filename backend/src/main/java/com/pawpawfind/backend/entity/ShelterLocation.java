package com.pawpawfind.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "shelter_locations", uniqueConstraints =
		@UniqueConstraint(name = "uk_shelter_locations_shelter_key", columnNames = "shelter_key"))
public class ShelterLocation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "shelter_key", length = 80, nullable = false)
	private String shelterKey;

	@Column(name = "care_reg_no", length = 30)
	private String careRegNo;

	@Column(name = "care_nm", length = 100)
	private String careNm;

	@Column(name = "care_tel", length = 30)
	private String careTel;

	@Column(name = "care_addr", length = 255)
	private String careAddr;

	@Column(name = "normalized_address", length = 255)
	private String normalizedAddress;

	@Column(name = "address_hash", length = 64)
	private String addressHash;

	@Column(name = "latitude")
	private Double latitude;

	@Column(name = "longitude")
	private Double longitude;

	@Enumerated(EnumType.STRING)
	@Column(name = "geocode_status", length = 20, nullable = false)
	private GeocodeStatus geocodeStatus;

	@Column(name = "geocode_provider", length = 20)
	private String geocodeProvider;

	@Column(name = "attempt_count", nullable = false)
	private int attemptCount;

	@Column(name = "geocoded_at")
	private LocalDateTime geocodedAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	public void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
		if (geocodeStatus == null) geocodeStatus = GeocodeStatus.PENDING;
	}

	@PreUpdate
	public void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getShelterKey() { return shelterKey; }
	public void setShelterKey(String shelterKey) { this.shelterKey = shelterKey; }
	public String getCareRegNo() { return careRegNo; }
	public void setCareRegNo(String careRegNo) { this.careRegNo = careRegNo; }
	public String getCareNm() { return careNm; }
	public void setCareNm(String careNm) { this.careNm = careNm; }
	public String getCareTel() { return careTel; }
	public void setCareTel(String careTel) { this.careTel = careTel; }
	public String getCareAddr() { return careAddr; }
	public void setCareAddr(String careAddr) { this.careAddr = careAddr; }
	public String getNormalizedAddress() { return normalizedAddress; }
	public void setNormalizedAddress(String normalizedAddress) { this.normalizedAddress = normalizedAddress; }
	public String getAddressHash() { return addressHash; }
	public void setAddressHash(String addressHash) { this.addressHash = addressHash; }
	public Double getLatitude() { return latitude; }
	public void setLatitude(Double latitude) { this.latitude = latitude; }
	public Double getLongitude() { return longitude; }
	public void setLongitude(Double longitude) { this.longitude = longitude; }
	public GeocodeStatus getGeocodeStatus() { return geocodeStatus; }
	public void setGeocodeStatus(GeocodeStatus geocodeStatus) { this.geocodeStatus = geocodeStatus; }
	public String getGeocodeProvider() { return geocodeProvider; }
	public void setGeocodeProvider(String geocodeProvider) { this.geocodeProvider = geocodeProvider; }
	public int getAttemptCount() { return attemptCount; }
	public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
	public LocalDateTime getGeocodedAt() { return geocodedAt; }
	public void setGeocodedAt(LocalDateTime geocodedAt) { this.geocodedAt = geocodedAt; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
