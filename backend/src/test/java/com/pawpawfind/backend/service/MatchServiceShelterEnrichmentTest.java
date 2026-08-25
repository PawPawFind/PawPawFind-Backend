package com.pawpawfind.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.pawpawfind.backend.dto.MatchCandidateDto;
import com.pawpawfind.backend.dto.MatchResultUpsertRequest;
import com.pawpawfind.backend.entity.Animal;
import com.pawpawfind.backend.entity.MatchResult;
import com.pawpawfind.backend.entity.MatchRun;
import com.pawpawfind.backend.entity.Reports;
import com.pawpawfind.backend.repository.AnimalRepository;
import com.pawpawfind.backend.repository.MatchResultRepository;
import com.pawpawfind.backend.repository.MatchRunRepository;
import com.pawpawfind.backend.repository.ReportFeatureRepository;
import com.pawpawfind.backend.repository.ReportPhotoRepository;
import com.pawpawfind.backend.repository.ReportRepository;

class MatchServiceShelterEnrichmentTest {

	private MatchRunRepository matchRunRepository;
	private MatchResultRepository matchResultRepository;
	private ReportRepository reportRepository;
	private AnimalRepository animalRepository;
	private MatchCandidateShelterAssembler assembler;
	private MatchService service;

	@BeforeEach
	void setUp() {
		matchRunRepository = mock(MatchRunRepository.class);
		matchResultRepository = mock(MatchResultRepository.class);
		reportRepository = mock(ReportRepository.class);
		animalRepository = mock(AnimalRepository.class);
		assembler = mock(MatchCandidateShelterAssembler.class);
		service = new MatchService(matchRunRepository, matchResultRepository, reportRepository,
				mock(ReportPhotoRepository.class), mock(ReportFeatureRepository.class),
				animalRepository, assembler);
	}

	@Test
	void appliesLimitBeforeSharedEnrichmentAndPreservesOrderAndScores() {
		MatchRun run = run();
		when(reportRepository.existsById(14L)).thenReturn(true);
		when(matchRunRepository.findTopByReportIdAndStatusOrderByCreatedAtDesc(14L, "DONE"))
				.thenReturn(Optional.of(run));
		when(matchResultRepository.findByMatchRunIdOrderByRankAsc(9L)).thenReturn(List.of(
				result((short) 3, "SHELTER", "A-1", null, "0.91"),
				result((short) 8, "REPORT", null, 22L, "0.82"),
				result((short) 9, "SHELTER", "A-2", null, "0.73")));

		var response = service.getLatestMatches(14L, 2);

		assertThat(response.getResults()).extracting(MatchCandidateDto::getCandidateType)
				.containsExactly("SHELTER", "REPORT");
		assertThat(response.getResults()).extracting(MatchCandidateDto::getRank)
				.containsExactly((short) 1, (short) 2);
		assertThat(response.getResults()).extracting(MatchCandidateDto::getRankingScore)
				.containsExactly(new BigDecimal("0.91"), new BigDecimal("0.82"));
		ArgumentCaptor<List<MatchCandidateDto>> captor = candidateListCaptor();
		verify(assembler).enrich(captor.capture());
		assertThat(captor.getValue()).hasSize(2).isSameAs(response.getResults());
		verify(animalRepository, never()).existsById(org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	void enrichesEmptyResultWithoutRepositoryPerCandidateChecks() {
		when(reportRepository.existsById(14L)).thenReturn(true);
		when(matchRunRepository.findTopByReportIdAndStatusOrderByCreatedAtDesc(14L, "DONE"))
				.thenReturn(Optional.of(run()));
		when(matchResultRepository.findByMatchRunIdOrderByRankAsc(9L)).thenReturn(List.of());

		var response = service.getLatestMatches(14L, 20);

		assertThat(response.getResults()).isEmpty();
		verify(assembler).enrich(anyList());
		verify(animalRepository, never()).existsById(org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	void validatesCandidatesWithDeduplicatedBatchQueriesBeforeSaving() {
		MatchRun run = run();
		when(reportRepository.existsById(14L)).thenReturn(true);
		when(matchRunRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(run);
		when(matchRunRepository.findTopByReportIdAndStatusOrderByCreatedAtDesc(14L, "DONE"))
				.thenReturn(Optional.of(run));
		when(matchResultRepository.findByMatchRunIdOrderByRankAsc(9L)).thenReturn(List.of());
		Animal animal = new Animal();
		animal.setDesertionNo("A-1");
		when(animalRepository.findAllById(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(animal));
		Reports report = new Reports();
		report.setReportId(22L);
		when(reportRepository.findAllById(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(report));
		MatchCandidateDto shelter = candidate("SHELTER", "A-1", null);
		MatchCandidateDto duplicate = candidate("SHELTER", "A-1", null);
		MatchCandidateDto reportCandidate = candidate("REPORT", null, 22L);
		MatchCandidateDto missing = candidate("SHELTER", "missing", null);
		MatchResultUpsertRequest request = new MatchResultUpsertRequest();
		request.setReportId(14L);
		request.setModelVersion("model-v1");
		request.setDecision("REVIEW");
		request.setResults(List.of(shelter, duplicate, reportCandidate, missing));

		service.saveMatchResults(request);

		verify(animalRepository, times(1)).findAllById(org.mockito.ArgumentMatchers.any());
		verify(reportRepository, times(1)).findAllById(org.mockito.ArgumentMatchers.any());
		verify(animalRepository, never()).existsById(org.mockito.ArgumentMatchers.anyString());
		verify(matchResultRepository, times(3)).save(org.mockito.ArgumentMatchers.any());
	}

	@SuppressWarnings("unchecked")
	private ArgumentCaptor<List<MatchCandidateDto>> candidateListCaptor() {
		return ArgumentCaptor.forClass(List.class);
	}

	private MatchRun run() {
		MatchRun run = new MatchRun();
		run.setId(9L);
		run.setReportId(14L);
		run.setModelVersion("model-v1");
		run.setRerankVersion("rerank-v1");
		run.setDecision("REVIEW");
		run.setStatus("DONE");
		return run;
	}

	private MatchResult result(Short rank, String type, String desertionNo,
			Long candidateReportId, String rankingScore) {
		MatchResult result = new MatchResult();
		result.setRank(rank);
		result.setCandidateType(type);
		result.setDesertionNo(desertionNo);
		result.setCandidateReportId(candidateReportId);
		result.setVisualScore(new BigDecimal("0.50"));
		result.setRankingScore(new BigDecimal(rankingScore));
		result.setMatchedTags(java.util.Map.of("color", "brown"));
		result.setConflictingTags(java.util.Map.of());
		return result;
	}

	private MatchCandidateDto candidate(String type, String desertionNo, Long reportId) {
		MatchCandidateDto candidate = new MatchCandidateDto();
		candidate.setRank((short) 1);
		candidate.setCandidateType(type);
		candidate.setDesertionNo(desertionNo);
		candidate.setCandidateReportId(reportId);
		candidate.setVisualScore(new BigDecimal("0.5"));
		candidate.setNearDuplicate(false);
		return candidate;
	}
}
