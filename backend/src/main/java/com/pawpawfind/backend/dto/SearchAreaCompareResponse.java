package com.pawpawfind.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class SearchAreaCompareResponse {

	private Long reportId;
	@JsonFormat(shape = JsonFormat.Shape.STRING, without = JsonFormat.Feature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
	private OffsetDateTime comparedAt;
	private SearchAreaAlgorithmResponse baseline;
	private SearchAreaAlgorithmResponse improved;
	private SearchAreaComparisonResponse comparison;

	public Long getReportId() { return reportId; }
	public void setReportId(Long reportId) { this.reportId = reportId; }
	public OffsetDateTime getComparedAt() { return comparedAt; }
	public void setComparedAt(OffsetDateTime comparedAt) { this.comparedAt = comparedAt; }
	public SearchAreaAlgorithmResponse getBaseline() { return baseline; }
	public void setBaseline(SearchAreaAlgorithmResponse baseline) { this.baseline = baseline; }
	public SearchAreaAlgorithmResponse getImproved() { return improved; }
	public void setImproved(SearchAreaAlgorithmResponse improved) { this.improved = improved; }
	public SearchAreaComparisonResponse getComparison() { return comparison; }
	public void setComparison(SearchAreaComparisonResponse comparison) { this.comparison = comparison; }
}
