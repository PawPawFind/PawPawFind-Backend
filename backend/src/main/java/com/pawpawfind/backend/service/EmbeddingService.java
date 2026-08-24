package com.pawpawfind.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawpawfind.backend.dto.AnimalEmbeddingBatchRequest;
import com.pawpawfind.backend.dto.AnimalForEmbeddingResponse;
import com.pawpawfind.backend.dto.EmbeddingBatchResponse;
import com.pawpawfind.backend.dto.GallerySearchResponse;
import com.pawpawfind.backend.dto.MatchFeatureDto;
import com.pawpawfind.backend.dto.ReportEmbeddingBatchRequest;
import com.pawpawfind.backend.dto.ReportPhotoForEmbeddingResponse;
import com.pawpawfind.backend.entity.Animal;
import com.pawpawfind.backend.entity.AnimalEmbedding;
import com.pawpawfind.backend.entity.ReportEmbedding;
import com.pawpawfind.backend.entity.ReportFeatures;
import com.pawpawfind.backend.entity.ReportPhotos;
import com.pawpawfind.backend.entity.Reports;
import com.pawpawfind.backend.repository.AnimalEmbeddingRepository;
import com.pawpawfind.backend.repository.AnimalRepository;
import com.pawpawfind.backend.repository.ReportEmbeddingRepository;
import com.pawpawfind.backend.repository.ReportFeatureRepository;
import com.pawpawfind.backend.repository.ReportPhotoRepository;
import com.pawpawfind.backend.repository.ReportRepository;

@Service
public class EmbeddingService {

	private static final Map<String, String> SPECIES_KO_TO_EN = Map.of(
			"강아지", "dog",
			"고양이", "cat");
	private static final Map<String, String> UPKIND_CD_TO_EN = Map.of(
			"417000", "dog",
			"422400", "cat");
	private static final DateTimeFormatter ISO_LOCAL = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	private final AnimalEmbeddingRepository animalEmbeddingRepository;
	private final ReportEmbeddingRepository reportEmbeddingRepository;
	private final AnimalRepository animalRepository;
	private final ReportRepository reportRepository;
	private final ReportPhotoRepository reportPhotoRepository;
	private final ReportFeatureRepository reportFeatureRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public EmbeddingService(
			AnimalEmbeddingRepository animalEmbeddingRepository,
			ReportEmbeddingRepository reportEmbeddingRepository,
			AnimalRepository animalRepository,
			ReportRepository reportRepository,
			ReportPhotoRepository reportPhotoRepository,
			ReportFeatureRepository reportFeatureRepository) {
		this.animalEmbeddingRepository = animalEmbeddingRepository;
		this.reportEmbeddingRepository = reportEmbeddingRepository;
		this.animalRepository = animalRepository;
		this.reportRepository = reportRepository;
		this.reportPhotoRepository = reportPhotoRepository;
		this.reportFeatureRepository = reportFeatureRepository;
	}

	@Transactional
	public EmbeddingBatchResponse upsertAnimalEmbeddings(AnimalEmbeddingBatchRequest request) {
		if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
			throw new IllegalArgumentException("items must not be empty");
		}

		int upserted = 0;
		int skipped = 0;
		for (AnimalEmbeddingBatchRequest.AnimalEmbeddingBatchItem item : request.getItems()) {
			if (item.getGalleryId() == null || item.getDesertionNo() == null) {
				skipped++;
				continue;
			}
			if (item.getEmbeddingFull() == null || item.getEmbeddingCrop() == null) {
				skipped++;
				continue;
			}

			AnimalEmbedding row = Optional.ofNullable(animalEmbeddingRepository.findByGalleryId(item.getGalleryId()))
					.orElseGet(AnimalEmbedding::new);
			row.setDesertionNo(item.getDesertionNo());
			row.setGalleryId(item.getGalleryId());
			row.setSpecies(normalizeSpecies(item.getSpecies()));
			row.setPhotoIndex(item.getPhotoIndex() == null ? (short) 0 : item.getPhotoIndex());
			row.setImageUrl(item.getImageUrl());
			row.setPhashFull(item.getPhashFull());
			row.setPhashCrop(item.getPhashCrop());
			row.setModelVersion(item.getModelVersion());
			row.setPreprocessVersion(item.getPreprocessVersion());
			row.setDetectionConfidence(item.getDetectionConfidence());
			row.setBlurScore(item.getBlurScore());
			row.setEmbeddingRef(serializeEmbeddingRef(item.getEmbeddingFull(), item.getEmbeddingCrop()));
			animalEmbeddingRepository.save(row);
			upserted++;
		}
		return new EmbeddingBatchResponse(upserted, skipped);
	}

	@Transactional
	public EmbeddingBatchResponse upsertReportEmbeddings(ReportEmbeddingBatchRequest request) {
		if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
			throw new IllegalArgumentException("items must not be empty");
		}

		int upserted = 0;
		int skipped = 0;
		for (ReportEmbeddingBatchRequest.ReportEmbeddingBatchItem item : request.getItems()) {
			if (item.getReportPhotoId() == null || item.getReportId() == null) {
				skipped++;
				continue;
			}
			if (item.getEmbeddingFull() == null || item.getEmbeddingCrop() == null) {
				skipped++;
				continue;
			}
			if (!reportPhotoRepository.existsById(item.getReportPhotoId())) {
				skipped++;
				continue;
			}

			ReportEmbedding row = Optional
					.ofNullable(reportEmbeddingRepository.findByReportPhotoId(item.getReportPhotoId()))
					.orElseGet(ReportEmbedding::new);
			row.setReportPhotoId(item.getReportPhotoId());
			row.setReportId(item.getReportId());
			row.setPhashFull(item.getPhashFull());
			row.setPhashCrop(item.getPhashCrop());
			row.setModelVersion(item.getModelVersion());
			row.setPreprocessVersion(item.getPreprocessVersion());
			row.setEmbeddingRef(serializeEmbeddingRef(item.getEmbeddingFull(), item.getEmbeddingCrop()));
			reportEmbeddingRepository.save(row);
			upserted++;
		}
		return new EmbeddingBatchResponse(upserted, skipped);
	}

	public GallerySearchResponse getGalleryForSearch(
			String speciesKo,
			String modelVersion,
			String preprocessVersion,
			Long excludeReportId,
			boolean includeAnimals,
			boolean includeReports) {
		String speciesEn = SPECIES_KO_TO_EN.get(speciesKo);
		if (speciesEn == null) {
			throw new IllegalArgumentException("Unsupported species: " + speciesKo);
		}
		if (modelVersion == null || modelVersion.isBlank()) {
			throw new IllegalArgumentException("modelVersion is required");
		}
		if (preprocessVersion == null || preprocessVersion.isBlank()) {
			throw new IllegalArgumentException("preprocessVersion is required");
		}

		GallerySearchResponse response = new GallerySearchResponse();
		response.setModelVersion(modelVersion);
		response.setPreprocessVersion(preprocessVersion);

		if (includeAnimals) {
			response.setAnimals(buildAnimalItems(speciesEn, modelVersion, preprocessVersion));
		} else {
			response.setAnimals(List.of());
		}

		if (includeReports) {
			response.setReports(buildReportItems(speciesKo, modelVersion, preprocessVersion, excludeReportId));
		} else {
			response.setReports(List.of());
		}
		return response;
	}

	public AnimalForEmbeddingResponse getAnimalsForEmbedding(LocalDateTime since, boolean missingOnly) {
		List<Animal> animals = missingOnly
				? animalRepository.findAnimalsMissingEmbeddings()
				: (since == null
						? animalRepository.findAll()
						: animalRepository.findByUpdatedAtAfterOrderByUpdatedAtAsc(since));

		List<AnimalForEmbeddingResponse.AnimalForEmbeddingItem> items = new ArrayList<>();
		for (Animal animal : animals) {
			List<String> photoUrls = collectPhotoUrls(animal.getPopfile1(), animal.getPopfile2());
			if (photoUrls.isEmpty()) {
				continue;
			}
			String species = resolveAnimalSpecies(animal);
			if (species == null) {
				continue;
			}

			AnimalForEmbeddingResponse.AnimalForEmbeddingItem item =
					new AnimalForEmbeddingResponse.AnimalForEmbeddingItem();
			item.setDesertionNo(animal.getDesertionNo());
			item.setSpecies(species);
			item.setPhotoUrls(photoUrls);
			item.setKindCd(animal.getKindCd());
			item.setKindNm(animal.getKindNm());
			item.setColorCd(animal.getColorCd());
			item.setSexCd(animal.getSexCd());
			item.setCareNm(animal.getCareNm());
			item.setCareTel(animal.getCareTel());
			item.setCareAddr(animal.getCareAddr());
			item.setSpecialMark(animal.getSpecialMark());
			if (animal.getUpdatedAt() != null) {
				item.setUpdatedAt(animal.getUpdatedAt().format(ISO_LOCAL));
			}
			items.add(item);
		}

		AnimalForEmbeddingResponse response = new AnimalForEmbeddingResponse();
		response.setItems(items);
		return response;
	}

	public ReportPhotoForEmbeddingResponse getReportPhotosForEmbedding(boolean missingOnly) {
		List<ReportPhotos> photos = missingOnly
				? reportPhotoRepository.findPhotosMissingEmbeddings()
				: reportPhotoRepository.findAll();

		List<ReportPhotoForEmbeddingResponse.ReportPhotoForEmbeddingItem> items = new ArrayList<>();
		for (ReportPhotos photo : photos) {
			Optional<Reports> report = reportRepository.findById(photo.getReportId());
			if (report.isEmpty()) {
				continue;
			}
			String speciesKo = report.get().getSpecies();
			String species = SPECIES_KO_TO_EN.get(speciesKo);
			if (species == null) {
				continue;
			}
			if (photo.getPhotoUrl() == null || photo.getPhotoUrl().isBlank()) {
				continue;
			}

			ReportPhotoForEmbeddingResponse.ReportPhotoForEmbeddingItem item =
					new ReportPhotoForEmbeddingResponse.ReportPhotoForEmbeddingItem();
			item.setReportPhotoId(photo.getId());
			item.setReportId(photo.getReportId());
			item.setSpecies(species);
			item.setPhotoUrl(photo.getPhotoUrl());
			items.add(item);
		}

		ReportPhotoForEmbeddingResponse response = new ReportPhotoForEmbeddingResponse();
		response.setItems(items);
		return response;
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

	private String resolveAnimalSpecies(Animal animal) {
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

	private List<GallerySearchResponse.GalleryAnimalItem> buildAnimalItems(
			String speciesEn, String modelVersion, String preprocessVersion) {
		List<AnimalEmbedding> rows = animalEmbeddingRepository.findBySpeciesAndModelVersionAndPreprocessVersion(
				speciesEn, modelVersion, preprocessVersion);
		List<GallerySearchResponse.GalleryAnimalItem> items = new ArrayList<>();
		for (AnimalEmbedding row : rows) {
			// animals에 없는 orphan embedding은 갤러리에서 제외 (삭제/만료 공고)
			if (!animalRepository.existsById(row.getDesertionNo())) {
				continue;
			}
			EmbeddingVectors vectors = parseEmbeddingRef(row.getEmbeddingRef());
			if (vectors == null) {
				continue;
			}

			GallerySearchResponse.GalleryAnimalItem item = new GallerySearchResponse.GalleryAnimalItem();
			item.setGalleryId(row.getGalleryId());
			item.setDesertionNo(row.getDesertionNo());
			item.setSpecies(row.getSpecies());
			item.setImageUrl(row.getImageUrl());
			item.setPhashFull(row.getPhashFull());
			item.setPhashCrop(row.getPhashCrop());
			item.setEmbeddingFull(vectors.full());
			item.setEmbeddingCrop(vectors.crop());
			item.setDetectionConfidence(row.getDetectionConfidence());
			item.setBlurScore(row.getBlurScore());
			item.setMetadata(buildAnimalMetadata(row));
			items.add(item);
		}
		return items;
	}

	private Map<String, Object> buildAnimalMetadata(AnimalEmbedding row) {
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("gallery_id", row.getGalleryId());
		metadata.put("record_id", row.getDesertionNo());
		metadata.put("species", row.getSpecies());
		metadata.put("image_url", row.getImageUrl());
		metadata.put("candidate_type", "SHELTER");

		Optional<Animal> animal = animalRepository.findById(row.getDesertionNo());
		animal.ifPresent(value -> {
			metadata.put("kindCd", value.getKindCd());
			metadata.put("kindNm", value.getKindNm());
			metadata.put("colorCd", value.getColorCd());
			metadata.put("sexCd", value.getSexCd());
			metadata.put("careNm", value.getCareNm());
			metadata.put("careTel", value.getCareTel());
			metadata.put("careAddr", value.getCareAddr());
			metadata.put("specialMark", value.getSpecialMark());
		});
		return metadata;
	}

	private List<GallerySearchResponse.GalleryReportItem> buildReportItems(
			String speciesKo,
			String modelVersion,
			String preprocessVersion,
			Long excludeReportId) {
		List<ReportEmbedding> rows = reportEmbeddingRepository.findForGallerySearch(
				speciesKo, modelVersion, preprocessVersion, excludeReportId);
		List<GallerySearchResponse.GalleryReportItem> items = new ArrayList<>();
		for (ReportEmbedding row : rows) {
			EmbeddingVectors vectors = parseEmbeddingRef(row.getEmbeddingRef());
			if (vectors == null) {
				continue;
			}

			Optional<ReportPhotos> photo = reportPhotoRepository.findById(row.getReportPhotoId());
			if (photo.isEmpty()) {
				continue;
			}
			Optional<Reports> report = reportRepository.findById(row.getReportId());
			if (report.isEmpty()) {
				continue;
			}

			GallerySearchResponse.GalleryReportItem item = new GallerySearchResponse.GalleryReportItem();
			item.setGalleryId("report:" + row.getReportId() + ":" + row.getReportPhotoId());
			item.setReportId(row.getReportId());
			item.setReportPhotoId(row.getReportPhotoId());
			item.setSpecies(SPECIES_KO_TO_EN.getOrDefault(report.get().getSpecies(), "dog"));
			item.setImageUrl(photo.get().getPhotoUrl());
			item.setPhashFull(row.getPhashFull());
			item.setPhashCrop(row.getPhashCrop());
			item.setEmbeddingFull(vectors.full());
			item.setEmbeddingCrop(vectors.crop());
			item.setFeatures(buildReportFeatures(row.getReportId()));
			items.add(item);
		}
		return items;
	}

	private List<MatchFeatureDto> buildReportFeatures(Long reportId) {
		List<ReportFeatures> rows = reportFeatureRepository.findByReportId(reportId);
		List<MatchFeatureDto> features = new ArrayList<>();
		for (ReportFeatures row : rows) {
			MatchFeatureDto feature = new MatchFeatureDto();
			feature.setCategory(row.getCategory());
			feature.setKeyword(row.getKeyword());
			features.add(feature);
		}
		return features;
	}

	private String normalizeSpecies(String species) {
		if (species == null) {
			throw new IllegalArgumentException("species is required");
		}
		String trimmed = species.trim().toLowerCase();
		if ("dog".equals(trimmed) || "cat".equals(trimmed)) {
			return trimmed;
		}
		String mapped = SPECIES_KO_TO_EN.get(species.trim());
		if (mapped != null) {
			return mapped;
		}
		throw new IllegalArgumentException("Unsupported species: " + species);
	}

	private String serializeEmbeddingRef(List<Double> full, List<Double> crop) {
		Map<String, List<Double>> payload = Map.of("full", full, "crop", crop);
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Failed to serialize embedding vectors", e);
		}
	}

	private EmbeddingVectors parseEmbeddingRef(String embeddingRef) {
		if (embeddingRef == null || embeddingRef.isBlank()) {
			return null;
		}
		try {
			Map<String, List<Double>> payload = objectMapper.readValue(
					embeddingRef, new TypeReference<Map<String, List<Double>>>() {
					});
			List<Double> full = payload.get("full");
			List<Double> crop = payload.get("crop");
			if (full == null || crop == null || full.isEmpty() || crop.isEmpty()) {
				return null;
			}
			return new EmbeddingVectors(full, crop);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	private record EmbeddingVectors(List<Double> full, List<Double> crop) {
	}
}
