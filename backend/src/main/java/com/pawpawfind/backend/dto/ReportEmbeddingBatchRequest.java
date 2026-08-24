package com.pawpawfind.backend.dto;

import java.util.List;

/** AI batch → BE report_embeddings upsert body. */
public class ReportEmbeddingBatchRequest {

	private List<ReportEmbeddingBatchItem> items;

	public List<ReportEmbeddingBatchItem> getItems() {
		return items;
	}

	public void setItems(List<ReportEmbeddingBatchItem> items) {
		this.items = items;
	}

	public static class ReportEmbeddingBatchItem {
		private Long reportPhotoId;
		private Long reportId;
		private String phashFull;
		private String phashCrop;
		private String modelVersion;
		private String preprocessVersion;
		private List<Double> embeddingFull;
		private List<Double> embeddingCrop;

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
	}
}
