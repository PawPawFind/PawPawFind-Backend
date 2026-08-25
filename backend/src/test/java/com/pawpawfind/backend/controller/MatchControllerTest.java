package com.pawpawfind.backend.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.pawpawfind.backend.dto.MatchCandidateDto;
import com.pawpawfind.backend.dto.MatchQueryResponse;
import com.pawpawfind.backend.dto.MatchShelterDto;
import com.pawpawfind.backend.service.MatchService;

class MatchControllerTest {

	private MatchService matchService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		matchService = mock(MatchService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new MatchController(matchService)).build();
	}

	@Test
	void runMatchReturnsShelterAndPreservesExistingCandidateContractWithoutAuthentication() throws Exception {
		when(matchService.runMatch(14L)).thenReturn(response());

		assertCompleteContract(mockMvc.perform(post("/api/reports/14/run-match")));
	}

	@Test
	void latestMatchesReturnsTheSameShelterCandidateContractWithoutAuthentication() throws Exception {
		when(matchService.getLatestMatches(14L, 20)).thenReturn(response());

		assertCompleteContract(mockMvc.perform(get("/api/reports/14/matches")));
	}

	@Test
	void reportCandidateReturnsNullShelter() throws Exception {
		MatchQueryResponse response = response();
		MatchCandidateDto report = new MatchCandidateDto();
		report.setRank((short) 2);
		report.setCandidateType("REPORT");
		report.setCandidateReportId(22L);
		response.setResults(List.of(response.getResults().get(0), report));
		when(matchService.getLatestMatches(14L, 20)).thenReturn(response);

		mockMvc.perform(get("/api/reports/14/matches"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.results[1].candidateType").value("REPORT"))
				.andExpect(jsonPath("$.results[1].candidateReportId").value(22))
				.andExpect(jsonPath("$.results[1].shelter").value(org.hamcrest.Matchers.nullValue()));
	}

	@Test
	void preservesExistingNotFoundAndServiceUnavailableResponses() throws Exception {
		when(matchService.getLatestMatches(99L, 20)).thenReturn(null);
		when(matchService.runMatch(14L)).thenThrow(new IllegalStateException("internal AI URL"));

		mockMvc.perform(get("/api/reports/99/matches")).andExpect(status().isNotFound());
		mockMvc.perform(post("/api/reports/14/run-match"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$").doesNotExist());
	}

	private void assertCompleteContract(ResultActions result) throws Exception {
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.matchRunId").value(9))
				.andExpect(jsonPath("$.reportId").value(14))
				.andExpect(jsonPath("$.modelVersion").value("model-v1"))
				.andExpect(jsonPath("$.rerankVersion").value("rerank-v1"))
				.andExpect(jsonPath("$.decision").value("REVIEW"))
				.andExpect(jsonPath("$.status").value("DONE"))
				.andExpect(jsonPath("$.results[0].candidateType").value("SHELTER"))
				.andExpect(jsonPath("$.results[0].rankingScore").value(0.91))
				.andExpect(jsonPath("$.results[0].matchedTags.color").value("brown"))
				.andExpect(jsonPath("$.results[0].conflictingTags.size").value("large"))
				.andExpect(jsonPath("$.results[0].shelter.careRegNo").value("REG-1"))
				.andExpect(jsonPath("$.results[0].shelter.name").value("행복 보호소"))
				.andExpect(jsonPath("$.results[0].shelter.address").value("서울시 송파구"))
				.andExpect(jsonPath("$.results[0].shelter.telephone").value("02-1234"))
				.andExpect(jsonPath("$.results[0].shelter.latitude").value(37.5))
				.andExpect(jsonPath("$.results[0].shelter.longitude").value(127.1));
	}

	private MatchQueryResponse response() {
		MatchShelterDto shelter = new MatchShelterDto();
		shelter.setCareRegNo("REG-1");
		shelter.setName("행복 보호소");
		shelter.setAddress("서울시 송파구");
		shelter.setTelephone("02-1234");
		shelter.setLatitude(37.5);
		shelter.setLongitude(127.1);

		MatchCandidateDto candidate = new MatchCandidateDto();
		candidate.setRank((short) 1);
		candidate.setCandidateType("SHELTER");
		candidate.setDesertionNo("A-1");
		candidate.setVisualScore(new BigDecimal("0.80"));
		candidate.setRankingScore(new BigDecimal("0.91"));
		candidate.setMatchedTags(Map.of("color", "brown"));
		candidate.setConflictingTags(Map.of("size", "large"));
		candidate.setImageUrl("https://example.com/a.jpg");
		candidate.setShelter(shelter);

		MatchQueryResponse response = new MatchQueryResponse();
		response.setMatchRunId(9L);
		response.setReportId(14L);
		response.setModelVersion("model-v1");
		response.setRerankVersion("rerank-v1");
		response.setDecision("REVIEW");
		response.setStatus("DONE");
		response.setResults(List.of(candidate));
		return response;
	}
}
