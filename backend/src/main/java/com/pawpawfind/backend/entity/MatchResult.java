package com.pawpawfind.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * AI 매칭 Top-N 후보 1행. SearchMatch 1건 = DB 1행.
 */
@Entity
@Table(name = "match_results")
public class MatchResult {

	public static final String CANDIDATE_SHELTER = "SHELTER";
	public static final String CANDIDATE_REPORT = "REPORT";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "match_run_id", nullable = false)
	private Long matchRunId;

	@Column(name = "rank", nullable = false)
	private Short rank;

	@Column(name = "candidate_type", length = 10, nullable = false)
	private String candidateType;

	@Column(name = "desertion_no", length = 20)
	private String desertionNo;

	@Column(name = "candidate_report_id")
	private Long candidateReportId;

	@Column(name = "visual_score", precision = 8, scale = 5, nullable = false)
	private BigDecimal visualScore;

	@Column(name = "ranking_score", precision = 8, scale = 5)
	private BigDecimal rankingScore;

	@Column(name = "tag_score", precision = 8, scale = 5)
	private BigDecimal tagScore;

	@Column(name = "text_score", precision = 8, scale = 5)
	private BigDecimal textScore;

	@Column(name = "phash_distance")
	private Short phashDistance;

	@Column(name = "near_duplicate", nullable = false)
	private Boolean nearDuplicate;

	@Column(name = "matched_tags", columnDefinition = "jsonb")
	@JdbcTypeCode(SqlTypes.JSON)
	private Map<String, Object> matchedTags;

	@Column(name = "conflicting_tags", columnDefinition = "jsonb")
	@JdbcTypeCode(SqlTypes.JSON)
	private Map<String, Object> conflictingTags;

	@Column(name = "gallery_id", length = 100)
	private String galleryId;

	@Column(name = "image_url", columnDefinition = "TEXT")
	private String imageUrl;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
		if (this.nearDuplicate == null) {
			this.nearDuplicate = false;
		}
		validateCandidate();
	}

	@PreUpdate
	public void onUpdate() {
		validateCandidate();
	}

	private void validateCandidate() {
		if (CANDIDATE_SHELTER.equals(candidateType)) {
			if (desertionNo == null || desertionNo.isBlank()) {
				throw new IllegalStateException("SHELTER candidate requires desertionNo");
			}
			if (candidateReportId != null) {
				throw new IllegalStateException("SHELTER candidate must not have candidateReportId");
			}
			return;
		}
		if (CANDIDATE_REPORT.equals(candidateType)) {
			if (candidateReportId == null) {
				throw new IllegalStateException("REPORT candidate requires candidateReportId");
			}
			if (desertionNo != null && !desertionNo.isBlank()) {
				throw new IllegalStateException("REPORT candidate must not have desertionNo");
			}
			return;
		}
		throw new IllegalStateException("candidateType must be SHELTER or REPORT");
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMatchRunId() {
		return matchRunId;
	}

	public void setMatchRunId(Long matchRunId) {
		this.matchRunId = matchRunId;
	}

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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
