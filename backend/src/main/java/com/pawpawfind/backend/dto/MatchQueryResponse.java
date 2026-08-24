package com.pawpawfind.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /api/reports/{reportId}/matches 응답.
 */
public class MatchQueryResponse {

	private Long matchRunId;
	private Long reportId;
	private String modelVersion;
	private String rerankVersion;
	private String decision;
	private String status;
	private LocalDateTime createdAt;
	private List<MatchCandidateDto> results;

	public Long getMatchRunId() {
		return matchRunId;
	}

	public void setMatchRunId(Long matchRunId) {
		this.matchRunId = matchRunId;
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

	public List<MatchCandidateDto> getResults() {
		return results;
	}

	public void setResults(List<MatchCandidateDto> results) {
		this.results = results;
	}
}
