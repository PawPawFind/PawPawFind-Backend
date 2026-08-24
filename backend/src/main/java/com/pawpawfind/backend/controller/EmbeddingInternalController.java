package com.pawpawfind.backend.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pawpawfind.backend.dto.AnimalEmbeddingBatchRequest;
import com.pawpawfind.backend.dto.AnimalForEmbeddingResponse;
import com.pawpawfind.backend.dto.EmbeddingBatchResponse;
import com.pawpawfind.backend.dto.GallerySearchResponse;
import com.pawpawfind.backend.dto.ReportEmbeddingBatchRequest;
import com.pawpawfind.backend.dto.ReportPhotoForEmbeddingResponse;
import com.pawpawfind.backend.service.EmbeddingService;

/**
 * AI batch job 및 /match 갤러리 export용 internal API.
 */
@RestController
public class EmbeddingInternalController {

	private final EmbeddingService embeddingService;

	public EmbeddingInternalController(EmbeddingService embeddingService) {
		this.embeddingService = embeddingService;
	}

	@PostMapping("/api/internal/animal-embeddings/batch")
	public ResponseEntity<EmbeddingBatchResponse> upsertAnimalEmbeddings(
			@RequestBody AnimalEmbeddingBatchRequest request) {
		try {
			return ResponseEntity.ok(embeddingService.upsertAnimalEmbeddings(request));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping("/api/internal/report-embeddings/batch")
	public ResponseEntity<EmbeddingBatchResponse> upsertReportEmbeddings(
			@RequestBody ReportEmbeddingBatchRequest request) {
		try {
			return ResponseEntity.ok(embeddingService.upsertReportEmbeddings(request));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/api/internal/gallery/for-search")
	public ResponseEntity<GallerySearchResponse> getGalleryForSearch(
			@RequestParam String species,
			@RequestParam String modelVersion,
			@RequestParam String preprocessVersion,
			@RequestParam(required = false) Long excludeReportId,
			@RequestParam(defaultValue = "true") boolean includeAnimals,
			@RequestParam(defaultValue = "true") boolean includeReports) {
		try {
			GallerySearchResponse response = embeddingService.getGalleryForSearch(
					species,
					modelVersion,
					preprocessVersion,
					excludeReportId,
					includeAnimals,
					includeReports);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	/** AI batch: 변경·미적재 보호소 공고 export. */
	@GetMapping("/api/internal/animals/for-embedding")
	public ResponseEntity<AnimalForEmbeddingResponse> getAnimalsForEmbedding(
			@RequestParam(required = false) String since,
			@RequestParam(defaultValue = "false") boolean missingOnly) {
		try {
			LocalDateTime sinceTime = parseSince(since);
			return ResponseEntity.ok(embeddingService.getAnimalsForEmbedding(sinceTime, missingOnly));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	/** AI batch: embedding 없는 제보 사진 export. */
	@GetMapping("/api/internal/report-photos/for-embedding")
	public ResponseEntity<ReportPhotoForEmbeddingResponse> getReportPhotosForEmbedding(
			@RequestParam(defaultValue = "true") boolean missingOnly) {
		return ResponseEntity.ok(embeddingService.getReportPhotosForEmbedding(missingOnly));
	}

	private LocalDateTime parseSince(String since) {
		if (since == null || since.isBlank()) {
			return null;
		}
		try {
			return LocalDateTime.parse(since);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Invalid since timestamp: " + since);
		}
	}
}
