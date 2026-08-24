package com.pawpawfind.backend.entity;

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
 * 제보 사진별 Re-ID 벡터 참조. 매칭 쿼리용.
 */
@Entity
@Table(name = "report_embeddings")
public class ReportEmbedding {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "report_photo_id", nullable = false, unique = true)
	private Long reportPhotoId;

	@Column(name = "report_id", nullable = false)
	private Long reportId;

	@Column(name = "model_version", length = 50, nullable = false)
	private String modelVersion;

	@Column(name = "preprocess_version", length = 50, nullable = false)
	private String preprocessVersion;

	@Column(name = "phash_full", length = 32)
	private String phashFull;

	@Column(name = "phash_crop", length = 32)
	private String phashCrop;

	@Column(name = "embedding_ref", columnDefinition = "TEXT")
	private String embeddingRef;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	public void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	public void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getReportPhotoId() {
		return reportPhotoId;
	}

	public void setReportPhotoId(Long reportPhotoId) {
		this.reportPhotoId = reportPhotoId;
	}

	public Long getReportId() {
		return reportId;
	}

	public void setReportId(Long reportId) {
		this.reportId = reportId;
	}

	public String getModelVersion() {
		return modelVersion;
	}

	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}

	public String getPreprocessVersion() {
		return preprocessVersion;
	}

	public void setPreprocessVersion(String preprocessVersion) {
		this.preprocessVersion = preprocessVersion;
	}

	public String getPhashFull() {
		return phashFull;
	}

	public void setPhashFull(String phashFull) {
		this.phashFull = phashFull;
	}

	public String getPhashCrop() {
		return phashCrop;
	}

	public void setPhashCrop(String phashCrop) {
		this.phashCrop = phashCrop;
	}

	public String getEmbeddingRef() {
		return embeddingRef;
	}

	public void setEmbeddingRef(String embeddingRef) {
		this.embeddingRef = embeddingRef;
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
