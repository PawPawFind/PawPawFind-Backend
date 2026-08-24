package com.pawpawfind.backend.dto;

import java.math.BigDecimal;
import java.util.List;

/** AI batch → BE animal_embeddings upsert body. */
public class AnimalEmbeddingBatchRequest {

	private List<AnimalEmbeddingBatchItem> items;

	public List<AnimalEmbeddingBatchItem> getItems() {
		return items;
	}

	public void setItems(List<AnimalEmbeddingBatchItem> items) {
		this.items = items;
	}

	public static class AnimalEmbeddingBatchItem {
		private String desertionNo;
		private String galleryId;
		private String species;
		private Short photoIndex;
		private String imageUrl;
		private String phashFull;
		private String phashCrop;
		private String modelVersion;
		private String preprocessVersion;
		private List<Double> embeddingFull;
		private List<Double> embeddingCrop;
		private BigDecimal detectionConfidence;
		private BigDecimal blurScore;

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

		public List<Double> getEmbeddingFull() {
			return embeddingFull;
		}

		public void setEmbeddingFull(List<Double> embeddingFull) {
			this.embeddingFull = embeddingFull;
		}

		public List<Double> getEmbeddingCrop() {
			return embeddingCrop;
		}

		public void setEmbeddingCrop(List<Double> embeddingCrop) {
			this.embeddingCrop = embeddingCrop;
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
	}
}
