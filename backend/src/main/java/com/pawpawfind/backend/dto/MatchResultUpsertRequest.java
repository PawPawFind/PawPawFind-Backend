package com.pawpawfind.backend.dto;

import java.util.List;

/**
 * AI /match response 및 POST /api/internal/match-results body.
 */
public class MatchResultUpsertRequest {

	private Long reportId;
	private String modelVersion;
	private String rerankVersion;
	private String decision;
	private List<MatchCandidateDto> results;

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

	public String getRerankVersion() {
		return rerankVersion;
	}

	public void setRerankVersion(String rerankVersion) {
		this.rerankVersion = rerankVersion;
	}

	public String getDecision() {
		return decision;
	}

	public void setDecision(String decision) {
		this.decision = decision;
	}

	public List<MatchCandidateDto> getResults() {
		return results;
	}

	public void setResults(List<MatchCandidateDto> results) {
		this.results = results;
	}
}
