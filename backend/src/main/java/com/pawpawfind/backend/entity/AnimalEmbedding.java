package com.pawpawfind.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * 보호소 동물 사진 Re-ID 갤러리. AI batch로 적재한다.
 * pgvector 확장 시 embedding 컬럼을 추가할 수 있다.
 */
@Entity
@Table(
	name = "animal_embeddings",
	indexes = {
		@Index(name = "idx_animal_embeddings_desertion_no", columnList = "desertion_no"),
		@Index(name = "idx_animal_embeddings_model", columnList = "model_version, preprocess_version")
	}
)
public class AnimalEmbedding {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "desertion_no", length = 20, nullable = false)
	private String desertionNo;

	@Column(name = "gallery_id", length = 100, nullable = false, unique = true)
	private String galleryId;

	@Column(name = "species", length = 10, nullable = false)
	private String species;

	@Column(name = "photo_index", nullable = false)
	private Short photoIndex;

	@Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
	private String imageUrl;

	@Column(name = "phash_full", length = 32)
	private String phashFull;

	@Column(name = "phash_crop", length = 32)
	private String phashCrop;

	@Column(name = "model_version", length = 50, nullable = false)
	private String modelVersion;

	@Column(name = "preprocess_version", length = 50, nullable = false)
	private String preprocessVersion;

	@Column(name = "embedding_ref", columnDefinition = "TEXT")
	private String embeddingRef;

	@Column(name = "detection_confidence", precision = 6, scale = 4)
	private BigDecimal detectionConfidence;

	@Column(name = "blur_score", precision = 8, scale = 2)
	private BigDecimal blurScore;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	public void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
		if (this.photoIndex == null) {
			this.photoIndex = 0;
		}
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

	public String getDesertionNo() {
		return desertionNo;
	}

	public void setDesertionNo(String desertionNo) {
		this.desertionNo = desertionNo;
	}

	public String getGalleryId() {
		return galleryId;
	}

	public void setGalleryId(String galleryId) {
		this.galleryId = galleryId;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public Short getPhotoIndex() {
		return photoIndex;
	}

	public void setPhotoIndex(Short photoIndex) {
		this.photoIndex = photoIndex;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
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

	public String getEmbeddingRef() {
		return embeddingRef;
	}

	public void setEmbeddingRef(String embeddingRef) {
		this.embeddingRef = embeddingRef;
	}

	public BigDecimal getDetectionConfidence() {
		return detectionConfidence;
	}

	public void setDetectionConfidence(BigDecimal detectionConfidence) {
		this.detectionConfidence = detectionConfidence;
	}

	public BigDecimal getBlurScore() {
		return blurScore;
	}

	public void setBlurScore(BigDecimal blurScore) {
		this.blurScore = blurScore;
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
