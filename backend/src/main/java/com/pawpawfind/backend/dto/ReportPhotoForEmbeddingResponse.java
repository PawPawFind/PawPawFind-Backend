package com.pawpawfind.backend.dto;

import java.util.List;

/** BE → AI embedding batch export for report photos. */
public class ReportPhotoForEmbeddingResponse {

	private List<ReportPhotoForEmbeddingItem> items;

	public List<ReportPhotoForEmbeddingItem> getItems() {
		return items;
	}

	public void setItems(List<ReportPhotoForEmbeddingItem> items) {
		this.items = items;
	}

	public static class ReportPhotoForEmbeddingItem {
		private Long reportPhotoId;
		private Long reportId;
		private String species;
		private String photoUrl;

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

		public String getSpecies() {
			return species;
		}

		public void setSpecies(String species) {
			this.species = species;
		}

		public String getPhotoUrl() {
			return photoUrl;
		}

		public void setPhotoUrl(String photoUrl) {
			this.photoUrl = photoUrl;
		}
	}
}
