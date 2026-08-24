package com.pawpawfind.backend.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawpawfind.backend.entity.ReportPhotos;
import com.pawpawfind.backend.entity.Reports;
import com.pawpawfind.backend.repository.ReportRepository;

/**
 * 제보 사진 저장 직후 AI embed → report_embeddings (갤러리 후보 실시간 반영).
 * 실패해도 업로드 API는 성공. 6h embed-sync가 보험.
 */
@Service
public class ReportEmbedTriggerService {

	private static final Logger log = LoggerFactory.getLogger(ReportEmbedTriggerService.class);

	private static final Map<String, String> SPECIES_KO_TO_EN = Map.of(
			"강아지", "dog",
			"고양이", "cat");

	private final ReportRepository reportRepository;
	private final RestClient restClient;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Value("${ai.service.url:}")
	private String aiServiceUrl;

	public ReportEmbedTriggerService(ReportRepository reportRepository) {
		this.reportRepository = reportRepository;
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		this.restClient = RestClient.builder().requestFactory(requestFactory).build();
	}

	@Async
	public void triggerReportPhotoEmbed(ReportPhotos photo) {
		if (aiServiceUrl == null || aiServiceUrl.isBlank()) {
			return;
		}
		if (photo == null || photo.getId() == null || photo.getReportId() == null) {
			return;
		}
		String photoUrl = photo.getPhotoUrl();
		if (photoUrl == null || photoUrl.isBlank()) {
			return;
		}

		Reports report = reportRepository.findById(photo.getReportId()).orElse(null);
		if (report == null) {
			return;
		}
		String speciesEn = SPECIES_KO_TO_EN.get(report.getSpecies());
		if (speciesEn == null) {
			log.warn("Skip report photo embed: unsupported species {}", report.getSpecies());
			return;
		}

		Map<String, Object> body = new HashMap<>();
		body.put("reportPhotoId", photo.getId());
		body.put("reportId", photo.getReportId());
		body.put("photoUrl", photoUrl);
		body.put("species", speciesEn);

		try {
			String requestJson = objectMapper.writeValueAsString(body);
			restClient.post()
					.uri(aiServiceUrl + "/embed/report-photo")
					.contentType(MediaType.APPLICATION_JSON)
					.body(requestJson)
					.retrieve()
					.toBodilessEntity();
			log.info("Report photo embed triggered: reportPhotoId={}", photo.getId());
		} catch (JsonProcessingException e) {
			log.warn("Report photo embed JSON failed (reportPhotoId={}): {}", photo.getId(), e.getMessage());
		} catch (Exception e) {
			log.warn("Report photo embed failed (reportPhotoId={}): {}", photo.getId(), e.getMessage());
		}
	}
}
