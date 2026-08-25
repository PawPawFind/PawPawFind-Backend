package com.pawpawfind.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.pawpawfind.backend.dto.MatchAiRequest;
import com.pawpawfind.backend.dto.MatchCandidateDto;
import com.pawpawfind.backend.dto.MatchFeatureDto;
import com.pawpawfind.backend.dto.MatchQueryResponse;
import com.pawpawfind.backend.dto.MatchResultUpsertRequest;
import com.pawpawfind.backend.entity.MatchResult;
import com.pawpawfind.backend.entity.MatchRun;
import com.pawpawfind.backend.entity.ReportFeatures;
import com.pawpawfind.backend.entity.ReportPhotos;
import com.pawpawfind.backend.entity.Reports;
import com.pawpawfind.backend.repository.AnimalRepository;
import com.pawpawfind.backend.repository.MatchResultRepository;
import com.pawpawfind.backend.repository.MatchRunRepository;
import com.pawpawfind.backend.repository.ReportFeatureRepository;
import com.pawpawfind.backend.repository.ReportPhotoRepository;
import com.pawpawfind.backend.repository.ReportRepository;

/**
 * AI 매칭 결과 저장·조회 및 AI 서비스 호출.
 */
@Service
public class MatchService {

	private static final String STATUS_DONE = "DONE";

	private final MatchRunRepository matchRunRepository;
	private final MatchResultRepository matchResultRepository;
	private final ReportRepository reportRepository;
	private final ReportPhotoRepository reportPhotoRepository;
	private final ReportFeatureRepository reportFeatureRepository;
	private final AnimalRepository animalRepository;
	private final MatchCandidateShelterAssembler shelterAssembler;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final RestClient restClient;

	@Value("${ai.service.url:}")
	private String aiServiceUrl;

	public MatchService(
			MatchRunRepository matchRunRepository,
			MatchResultRepository matchResultRepository,
			ReportRepository reportRepository,
			ReportPhotoRepository reportPhotoRepository,
			ReportFeatureRepository reportFeatureRepository,
			AnimalRepository animalRepository,
			MatchCandidateShelterAssembler shelterAssembler) {
		this.matchRunRepository = matchRunRepository;
		this.matchResultRepository = matchResultRepository;
		this.reportRepository = reportRepository;
		this.reportPhotoRepository = reportPhotoRepository;
		this.reportFeatureRepository = reportFeatureRepository;
		this.animalRepository = animalRepository;
		this.shelterAssembler = shelterAssembler;
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		this.restClient = RestClient.builder().requestFactory(requestFactory).build();
	}

	@Transactional
	public MatchQueryResponse saveMatchResults(MatchResultUpsertRequest request) {
		if (request == null || request.getReportId() == null) {
			throw new IllegalArgumentException("reportId is required");
		}
		if (!reportRepository.existsById(request.getReportId())) {
			return null;
		}
		if (request.getResults() == null || request.getResults().isEmpty()) {
			throw new IllegalArgumentException("results must not be empty");
		}

		MatchRun run = new MatchRun();
		run.setReportId(request.getReportId());
		run.setModelVersion(request.getModelVersion());
		run.setRerankVersion(request.getRerankVersion());
		run.setDecision(request.getDecision());
		run.setStatus(STATUS_DONE);
		MatchRun savedRun = matchRunRepository.save(run);
		Set<String> existingAnimals = existingAnimalIds(request.getResults());
		Set<Long> existingReports = existingReportIds(request.getResults());

		for (MatchCandidateDto candidate : request.getResults()) {
			if (!isResolvableCandidate(candidate, existingAnimals, existingReports)) {
				continue;
			}
			MatchResult row = toEntity(savedRun.getId(), candidate);
			matchResultRepository.save(row);
		}

		return getLatestMatches(request.getReportId(), request.getResults().size());
	}

	public MatchQueryResponse getLatestMatches(Long reportId, int limit) {
		if (!reportRepository.existsById(reportId)) {
			return null;
		}

		return matchRunRepository
				.findTopByReportIdAndStatusOrderByCreatedAtDesc(reportId, STATUS_DONE)
				.map(run -> toResponse(run, limit))
				.orElseGet(() -> emptyResponse(reportId));
	}

	@Transactional
	public MatchQueryResponse runMatch(Long reportId) {
		if (aiServiceUrl == null || aiServiceUrl.isBlank()) {
			throw new IllegalStateException("ai.service.url is not configured");
		}

		Reports report = reportRepository.findById(reportId).orElse(null);
		if (report == null) {
			return null;
		}

		MatchAiRequest aiRequest = buildAiRequest(report);
		String requestJson;
		try {
			requestJson = objectMapper.writeValueAsString(aiRequest);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Failed to serialize AI match request", e);
		}
		MatchResultUpsertRequest aiResponse = restClient.post()
				.uri(aiServiceUrl + "/match")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(requestJson)
				.retrieve()
				.body(MatchResultUpsertRequest.class);

		if (aiResponse == null) {
			throw new IllegalStateException("AI service returned empty response");
		}
		if (aiResponse.getReportId() == null) {
			aiResponse.setReportId(reportId);
		}

		return saveMatchResults(aiResponse);
	}

	private MatchQueryResponse emptyResponse(Long reportId) {
		MatchQueryResponse response = new MatchQueryResponse();
		response.setReportId(reportId);
		response.setResults(List.of());
		return response;
	}

	private MatchAiRequest buildAiRequest(Reports report) {
		List<ReportPhotos> photos = reportPhotoRepository.findByReportId(report.getReportId());
		photos.sort(Comparator.comparing(
				ReportPhotos::getSortOrder,
				Comparator.nullsLast(Comparator.naturalOrder())));

		List<String> photoUrls = photos.stream()
				.map(ReportPhotos::getPhotoUrl)
				.collect(Collectors.toList());

		List<MatchFeatureDto> features = new ArrayList<>();
		for (ReportFeatures feature : reportFeatureRepository.findByReportId(report.getReportId())) {
			MatchFeatureDto dto = new MatchFeatureDto();
			dto.setCategory(feature.getCategory());
			dto.setKeyword(feature.getKeyword());
			features.add(dto);
		}

		MatchAiRequest request = new MatchAiRequest();
		request.setReportId(report.getReportId());
		request.setSpecies(report.getSpecies());
		request.setPhotoUrls(photoUrls);
		request.setFeatures(features);
		return request;
	}

	private MatchResult toEntity(Long matchRunId, MatchCandidateDto candidate) {
		MatchResult row = new MatchResult();
		row.setMatchRunId(matchRunId);
		row.setRank(candidate.getRank());
		row.setCandidateType(candidate.getCandidateType());
		row.setDesertionNo(candidate.getDesertionNo());
		row.setCandidateReportId(candidate.getCandidateReportId());
		row.setVisualScore(candidate.getVisualScore());
		row.setRankingScore(candidate.getRankingScore());
		row.setTagScore(candidate.getTagScore());
		row.setTextScore(candidate.getTextScore());
		row.setPhashDistance(candidate.getPhashDistance());
		row.setNearDuplicate(candidate.getNearDuplicate());
		row.setMatchedTags(candidate.getMatchedTags());
		row.setConflictingTags(candidate.getConflictingTags());
		row.setGalleryId(candidate.getGalleryId());
		row.setImageUrl(candidate.getImageUrl());
		return row;
	}

	private MatchQueryResponse toResponse(MatchRun run, int limit) {
		List<MatchResult> rows = matchResultRepository.findByMatchRunIdOrderByRankAsc(run.getId());
		List<MatchCandidateDto> results = rows.stream()
				.map(this::toDto)
				.collect(Collectors.toList());
		// 저장 시 검증된 순서를 유지하며 rank를 1..N으로 정규화한다.
		for (int i = 0; i < results.size(); i++) {
			results.get(i).setRank((short) (i + 1));
		}
		int effectiveLimit = limit > 0 ? Math.min(limit, results.size()) : results.size();
		if (effectiveLimit < results.size()) {
			results = new ArrayList<>(results.subList(0, effectiveLimit));
		}
		shelterAssembler.enrich(results);

		MatchQueryResponse response = new MatchQueryResponse();
		response.setMatchRunId(run.getId());
		response.setReportId(run.getReportId());
		response.setModelVersion(run.getModelVersion());
		response.setRerankVersion(run.getRerankVersion());
		response.setDecision(run.getDecision());
		response.setStatus(run.getStatus());
		response.setCreatedAt(run.getCreatedAt());
		response.setResults(results);
		return response;
	}

	/**
	 * SHELTER: animals에 desertionNo가 있어야 함.
	 * REPORT: reports에 candidateReportId가 있어야 함.
	 */
	private boolean isResolvableCandidate(MatchCandidateDto candidate,
			Set<String> existingAnimals, Set<Long> existingReports) {
		if (candidate == null || candidate.getCandidateType() == null) {
			return false;
		}
		if (MatchResult.CANDIDATE_SHELTER.equals(candidate.getCandidateType())) {
			String desertionNo = candidate.getDesertionNo();
			return desertionNo != null
					&& !desertionNo.isBlank()
					&& existingAnimals.contains(desertionNo);
		}
		if (MatchResult.CANDIDATE_REPORT.equals(candidate.getCandidateType())) {
			Long reportId = candidate.getCandidateReportId();
			return reportId != null && existingReports.contains(reportId);
		}
		return false;
	}

	private Set<String> existingAnimalIds(List<MatchCandidateDto> candidates) {
		Set<String> ids = candidates.stream()
				.filter(candidate -> candidate != null
						&& MatchResult.CANDIDATE_SHELTER.equals(candidate.getCandidateType()))
				.map(MatchCandidateDto::getDesertionNo)
				.filter(id -> id != null && !id.isBlank())
				.collect(Collectors.toSet());
		if (ids.isEmpty()) return Set.of();
		Set<String> existing = new HashSet<>();
		animalRepository.findAllById(ids).forEach(animal -> existing.add(animal.getDesertionNo()));
		return existing;
	}

	private Set<Long> existingReportIds(List<MatchCandidateDto> candidates) {
		Set<Long> ids = candidates.stream()
				.filter(candidate -> candidate != null
						&& MatchResult.CANDIDATE_REPORT.equals(candidate.getCandidateType()))
				.map(MatchCandidateDto::getCandidateReportId)
				.filter(java.util.Objects::nonNull)
				.collect(Collectors.toSet());
		if (ids.isEmpty()) return Set.of();
		Set<Long> existing = new HashSet<>();
		reportRepository.findAllById(ids).forEach(report -> existing.add(report.getReportId()));
		return existing;
	}

	private MatchCandidateDto toDto(MatchResult row) {
		MatchCandidateDto dto = new MatchCandidateDto();
		dto.setRank(row.getRank());
		dto.setCandidateType(row.getCandidateType());
		dto.setDesertionNo(row.getDesertionNo());
		dto.setCandidateReportId(row.getCandidateReportId());
		dto.setVisualScore(row.getVisualScore());
		dto.setRankingScore(row.getRankingScore());
		dto.setTagScore(row.getTagScore());
		dto.setTextScore(row.getTextScore());
		dto.setPhashDistance(row.getPhashDistance());
		dto.setNearDuplicate(row.getNearDuplicate());
		dto.setMatchedTags(row.getMatchedTags());
		dto.setConflictingTags(row.getConflictingTags());
		dto.setGalleryId(row.getGalleryId());
		dto.setImageUrl(row.getImageUrl());
		return dto;
	}
}
