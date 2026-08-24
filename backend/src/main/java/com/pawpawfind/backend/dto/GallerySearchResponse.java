package com.pawpawfind.backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** BE → AI gallery search export. */
public class GallerySearchResponse {

	private String modelVersion;
	private String preprocessVersion;
	private List<GalleryAnimalItem> animals;
	private List<GalleryReportItem> reports;

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

	public List<GalleryAnimalItem> getAnimals() {
		return animals;
	}

	public void setAnimals(List<GalleryAnimalItem> animals) {
		this.animals = animals;
	}

	public List<GalleryReportItem> getReports() {
		return reports;
	}

	public void setReports(List<GalleryReportItem> reports) {
		this.reports = reports;
	}

	public static class GalleryAnimalItem {
		private String galleryId;
		private String desertionNo;
		private String species;
		private String imageUrl;
		private String phashFull;
		private String phashCrop;
		private List<Double> embeddingFull;
		private List<Double> embeddingCrop;
		private BigDecimal detectionConfidence;
		private BigDecimal blurScore;
		private Map<String, Object> metadata;

		public String getGalleryId() {
			return galleryId;
		}

		public void setGalleryId(String galleryId) {
			this.galleryId = galleryId;
		}

		public String getDesertionNo() {
			return desertionNo;
		}

		public void setDesertionNo(String desertionNo) {
			this.desertionNo = desertionNo;
		}

		public String getSpecies() {
			return species;
		}

		public void setSpecies(String species) {
			this.species = species;
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

		public Map<String, Object> getMetadata() {
			return metadata;
		}

		public void setMetadata(Map<String, Object> metadata) {
			this.metadata = metadata;
		}
	}

	public static class GalleryReportItem {
		private String galleryId;
		private Long reportId;
		private Long reportPhotoId;
		private String species;
		private String imageUrl;
		private String phashFull;
		private String phashCrop;
		private List<Double> embeddingFull;
		private List<Double> embeddingCrop;
		private List<MatchFeatureDto> features;

		public String getGalleryId() {
			return galleryId;
		}

		public void setGalleryId(String galleryId) {
			this.galleryId = galleryId;
		}

		public Long getReportId() {
			return reportId;
		}

		public void setReportId(Long reportId) {
			this.reportId = reportId;
		}

		public Long getReportPhotoId() {
			return reportPhotoId;
		}

		public void setReportPhotoId(Long reportPhotoId) {
			this.reportPhotoId = reportPhotoId;
		}

		public String getSpecies() {
			return species;
		}

		public void setSpecies(String species) {
			this.species = species;
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

		public List<MatchFeatureDto> getFeatures() {
			return features;
		}

		public void setFeatures(List<MatchFeatureDto> features) {
			this.features = features;
		}
	}
}
