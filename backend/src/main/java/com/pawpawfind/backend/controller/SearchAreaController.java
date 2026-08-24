package com.pawpawfind.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import com.pawpawfind.backend.config.JwtAuthFilter;
import com.pawpawfind.backend.dto.SearchAreaResponse;
import com.pawpawfind.backend.service.SearchAreaService;

@RestController
public class SearchAreaController {

	private final SearchAreaService searchAreaService;

	public SearchAreaController(SearchAreaService searchAreaService) {
		this.searchAreaService = searchAreaService;
	}

	@PostMapping("/api/reports/{reportId}/search-areas")
	public ResponseEntity<SearchAreaResponse> recommend(
			@PathVariable Long reportId,
			@RequestAttribute(value = JwtAuthFilter.USER_ID_ATTR, required = false) Long userId,
			@RequestAttribute(value = JwtAuthFilter.ROLE_ATTR, required = false) String role) {
		return ResponseEntity.ok(searchAreaService.recommend(reportId, userId, role));
	}
}
