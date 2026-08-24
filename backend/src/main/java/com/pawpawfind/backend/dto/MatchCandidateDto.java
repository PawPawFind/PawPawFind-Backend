package com.pawpawfind.backend.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 매칭 후보 1건. AI /match response의 results[] 항목과 동일.
 */
public class MatchCandidateDto {

	private Short rank;
	private String candidateType;
	private String desertionNo;
	private Long candidateReportId;
	private BigDecimal visualScore;
	private BigDecimal rankingScore;
	private BigDecimal tagScore;
	private BigDecimal textScore;
	private Short phashDistance;
	private Boolean nearDuplicate;
	private Map<String, Object> matchedTags;
	private Map<String, Object> conflictingTags;
	private String galleryId;
	private String imageUrl;

	public Short getRank() {
		return rank;
	}

	public void setRank(Short rank) {
		this.rank = rank;
	}

	public String getCandidateType() {
		return candidateType;
	}

	public void setCandidateType(String candidateType) {
		this.candidateType = candidateType;
	}

	public String getDesertionNo() {
		return desertionNo;
	}

	public void setDesertionNo(String desertionNo) {
		this.desertionNo = desertionNo;
	}

	public Long getCandidateReportId() {
		return candidateReportId;
	}

	public void setCandidateReportId(Long candidateReportId) {
		this.candidateReportId = candidateReportId;
	}

	public BigDecimal getVisualScore() {
		return visualScore;
	}

	public void setVisualScore(BigDecimal visualScore) {
		this.visualScore = visualScore;
	}

	public BigDecimal getRankingScore() {
		return rankingScore;
	}

	public void setRankingScore(BigDecimal rankingScore) {
		this.rankingScore = rankingScore;
	}

	public BigDecimal getTagScore() {
		return tagScore;
	}

	public void setTagScore(BigDecimal tagScore) {
		this.tagScore = tagScore;
	}

	public BigDecimal getTextScore() {
		return textScore;
	}

	public void setTextScore(BigDecimal textScore) {
		this.textScore = textScore;
	}

	public Short getPhashDistance() {
		return phashDistance;
	}

	public void setPhashDistance(Short phashDistance) {
		this.phashDistance = phashDistance;
	}

	public Boolean getNearDuplicate() {
		return nearDuplicate;
	}

	public void setNearDuplicate(Boolean nearDuplicate) {
		this.nearDuplicate = nearDuplicate;
	}

	public Map<String, Object> getMatchedTags() {
		return matchedTags;
	}

	public void setMatchedTags(Map<String, Object> matchedTags) {
		this.matchedTags = matchedTags;
	}

	public Map<String, Object> getConflictingTags() {
		return conflictingTags;
	}

	public void setConflictingTags(Map<String, Object> conflictingTags) {
		this.conflictingTags = conflictingTags;
	}

	public String getGalleryId() {
		return galleryId;
	}

	public void setGalleryId(String galleryId) {
		this.galleryId = galleryId;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
}
