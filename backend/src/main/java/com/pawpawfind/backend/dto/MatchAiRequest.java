package com.pawpawfind.backend.dto;

import java.util.List;

/**
 * BE → AI POST /match 요청 body.
 */
public class MatchAiRequest {

	private Long reportId;
	private String species;
	private List<String> photoUrls;
	private List<MatchFeatureDto> features;

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

	public List<String> getPhotoUrls() {
		return photoUrls;
	}

	public void setPhotoUrls(List<String> photoUrls) {
		this.photoUrls = photoUrls;
	}

	public List<MatchFeatureDto> getFeatures() {
		return features;
	}

	public void setFeatures(List<MatchFeatureDto> features) {
		this.features = features;
	}
}
