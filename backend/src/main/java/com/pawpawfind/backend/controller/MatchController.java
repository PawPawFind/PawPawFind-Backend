package com.pawpawfind.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pawpawfind.backend.dto.MatchQueryResponse;
import com.pawpawfind.backend.dto.MatchResultUpsertRequest;
import com.pawpawfind.backend.service.MatchService;

/**
 * AI 매칭 결과 저장·조회 API.
 */
@RestController
public class MatchController {

	private final MatchService matchService;

	public MatchController(MatchService matchService) {
		this.matchService = matchService;
	}

	/** AI 서비스 또는 수동 테스트용. 매칭 결과를 RDS에 저장한다. */
	@PostMapping("/api/internal/match-results")
	public ResponseEntity<MatchQueryResponse> saveMatchResults(@RequestBody MatchResultUpsertRequest request) {
		try {
			MatchQueryResponse saved = matchService.saveMatchResults(request);
			if (saved == null) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(saved);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	/** FE용. 제보의 최신 매칭 Top-N 조회. */
	@GetMapping("/api/reports/{reportId}/matches")
	public ResponseEntity<MatchQueryResponse> getMatches(
			@PathVariable Long reportId,
			@RequestParam(defaultValue = "20") int limit) {
		MatchQueryResponse response = matchService.getLatestMatches(reportId, limit);
		if (response == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(response);
	}

	/** BE → AI /match 호출 후 결과 저장. ai.service.url 설정 필요. */
	@PostMapping("/api/reports/{reportId}/run-match")
	public ResponseEntity<MatchQueryResponse> runMatch(@PathVariable Long reportId) {
		try {
			MatchQueryResponse response = matchService.runMatch(reportId);
			if (response == null) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(response);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(503).body(null);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}
}
