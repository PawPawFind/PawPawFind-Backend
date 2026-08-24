package com.pawpawfind.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.pawpawfind.backend.entity.Animal;
import com.pawpawfind.backend.repository.AnimalEmbeddingRepository;

/**
 * 보호소 동물 sync 직후 AI embed → animal_embeddings (갤러리 후보 실시간 반영).
 */
@Service
public class AnimalEmbedTriggerService {

	private static final Logger log = LoggerFactory.getLogger(AnimalEmbedTriggerService.class);

	private static final Map<String, String> UPKIND_CD_TO_EN = Map.of(
			"417000", "dog",
			"422400", "cat");

	private final AnimalEmbeddingRepository animalEmbeddingRepository;
	private final RestClient restClient;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Value("${ai.service.url:}")
	private String aiServiceUrl;

	public AnimalEmbedTriggerService(AnimalEmbeddingRepository animalEmbeddingRepository) {
		this.animalEmbeddingRepository = animalEmbeddingRepository;
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		this.restClient = RestClient.builder().requestFactory(requestFactory).build();
	}

	public void triggerIfMissing(Animal animal) {
		if (animal == null || animal.getDesertionNo() == null || animal.getDesertionNo().isBlank()) {
			return;
		}
		if (animalEmbeddingRepository.existsByDesertionNo(animal.getDesertionNo())) {
			return;
		}
		triggerAnimalEmbed(animal);
	}

	@Async
	public void triggerAnimalEmbed(Animal animal) {
		if (aiServiceUrl == null || aiServiceUrl.isBlank()) {
			return;
		}
		if (animal == null || animal.getDesertionNo() == null || animal.getDesertionNo().isBlank()) {
			return;
		}

		String speciesEn = resolveSpecies(animal);
		if (speciesEn == null) {
			return;
		}

		List<String> photoUrls = collectPhotoUrls(animal.getPopfile1(), animal.getPopfile2());
		if (photoUrls.isEmpty()) {
			return;
		}

		Map<String, Object> body = new HashMap<>();
		body.put("desertionNo", animal.getDesertionNo());
		body.put("species", speciesEn);
		body.put("photoUrls", photoUrls);

		try {
			String requestJson = objectMapper.writeValueAsString(body);
			restClient.post()
					.uri(aiServiceUrl + "/embed/animal")
					.contentType(MediaType.APPLICATION_JSON)
					.body(requestJson)
					.retrieve()
					.toBodilessEntity();
			log.info("Animal embed triggered: desertionNo={}", animal.getDesertionNo());
		} catch (JsonProcessingException e) {
			log.warn("Animal embed JSON failed (desertionNo={}): {}", animal.getDesertionNo(), e.getMessage());
		} catch (Exception e) {
			log.warn("Animal embed failed (desertionNo={}): {}", animal.getDesertionNo(), e.getMessage());
		}
	}

	private String resolveSpecies(Animal animal) {
		if (animal.getUpKindCd() != null) {
			String mapped = UPKIND_CD_TO_EN.get(animal.getUpKindCd().trim());
			if (mapped != null) {
				return mapped;
			}
		}
		if (animal.getUpKindNm() != null) {
			String name = animal.getUpKindNm().trim();
			if (name.contains("개")) {
				return "dog";
			}
			if (name.contains("고양이") || name.contains("猫")) {
				return "cat";
			}
		}
		return null;
	}

	private List<String> collectPhotoUrls(String popfile1, String popfile2) {
		Set<String> urls = new LinkedHashSet<>();
		addPhotoUrl(urls, popfile1);
		addPhotoUrl(urls, popfile2);
		return new ArrayList<>(urls);
	}

	private void addPhotoUrl(Set<String> urls, String value) {
		if (value == null) {
			return;
		}
		String trimmed = value.trim();
		if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
			urls.add(trimmed);
		}
	}
}
