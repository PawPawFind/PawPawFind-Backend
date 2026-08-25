package com.pawpawfind.backend.service;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.pawpawfind.backend.entity.Animal;
import com.pawpawfind.backend.repository.AnimalRepository;

/**
 * 농림축산식품부 유기동물 공고(abandonmentPublic_v2) 수집.
 * Encoding 서비스키는 URI.create 로 붙여야 RestClient가 % 를 한 번 더 인코딩하지 않는다.
 * state=notice : 주인 찾는 공고만. 한 페이지 최대 1000마리.
 */
@Service
public class AbandonedService {
	private static final Logger log = LoggerFactory.getLogger(AbandonedService.class);
	private final AnimalRepository animalRepository;
	private final AnimalEmbedTriggerService animalEmbedTriggerService;
	private final ShelterLocationService shelterLocationService;
	private final ShelterGeocodingService shelterGeocodingService;
	private final ShelterIdentity shelterIdentity;
	private final RestClient restClient;

	@Autowired
	public AbandonedService(
			AnimalRepository animalRepository,
			AnimalEmbedTriggerService animalEmbedTriggerService,
			ShelterLocationService shelterLocationService,
			ShelterGeocodingService shelterGeocodingService,
			ShelterIdentity shelterIdentity) {
		this(animalRepository, animalEmbedTriggerService, shelterLocationService,
				shelterGeocodingService, shelterIdentity, RestClient.create());
	}

	AbandonedService(AnimalRepository animalRepository,
			AnimalEmbedTriggerService animalEmbedTriggerService,
			ShelterLocationService shelterLocationService,
			ShelterGeocodingService shelterGeocodingService,
			ShelterIdentity shelterIdentity,
			RestClient restClient) {
		this.animalRepository = animalRepository;
		this.animalEmbedTriggerService = animalEmbedTriggerService;
		this.shelterLocationService = shelterLocationService;
		this.shelterGeocodingService = shelterGeocodingService;
		this.shelterIdentity = shelterIdentity;
		this.restClient = restClient;
	}

	@Value("${animal.api.key}")
	private String apiKey;

	@Value("${animal.api.url}")
	private String apiUrl;

	public Object syncAbandonedAnimals() {
		int pageNo = 1;
		int pageSize = 1000;
		int totalCount = 0;
		int saved = 0;
		Set<String> synchronizedShelters = new HashSet<>();

		while (true) {
			URI uri = URI.create(apiUrl
					+ "?serviceKey=" + apiKey
					+ "&_type=json&pageNo=" + pageNo
					+ "&numOfRows=" + pageSize
					+ "&state=notice");

			Object raw = restClient.get()
					.uri(uri)
					.retrieve()
					.body(Object.class);

			Map<String, Object> root = (Map<String, Object>) raw;
			Map<String, Object> response = (Map<String, Object>) root.get("response");
			Map<String, Object> body = (Map<String, Object>) response.get("body");
			Map<String, Object> items = (Map<String, Object>) body.get("items");
			List<Map<String, Object>> itemList =
					(List<Map<String, Object>>) items.get("item");

			if (pageNo == 1) {
				totalCount = Integer.parseInt(String.valueOf(body.get("totalCount")));
			}

			for (Map<String, Object> animal : itemList) {
				Animal row = new Animal();
				row.setDesertionNo((String) animal.get("desertionNo"));
				row.setHappenDt((String) animal.get("happenDt"));
				row.setHappenPlace((String) animal.get("happenPlace"));
				row.setUpKindCd((String) animal.get("upKindCd"));
				row.setUpKindNm((String) animal.get("upKindNm"));
				row.setKindCd((String) animal.get("kindCd"));
				row.setKindNm((String) animal.get("kindNm"));
				row.setKindFullNm((String) animal.get("kindFullNm"));
				row.setColorCd((String) animal.get("colorCd"));
				row.setAge((String) animal.get("age"));
				row.setWeight((String) animal.get("weight"));
				row.setNoticeNo((String) animal.get("noticeNo"));
				row.setNoticeSdt((String) animal.get("noticeSdt"));
				row.setNoticeEdt((String) animal.get("noticeEdt"));
				row.setPopfile1((String) animal.get("popfile1"));
				row.setPopfile2((String) animal.get("popfile2"));
				row.setProcessState((String) animal.get("processState"));
				row.setSexCd((String) animal.get("sexCd"));
				row.setNeuterYn((String) animal.get("neuterYn"));
				row.setSpecialMark((String) animal.get("specialMark"));
				row.setCareRegNo((String) animal.get("careRegNo"));
				row.setCareNm((String) animal.get("careNm"));
				row.setCareTel((String) animal.get("careTel"));
				row.setCareAddr((String) animal.get("careAddr"));
				row.setOrgNm((String) animal.get("orgNm"));
				row.setSourceUpdTm((String) animal.get("updTm"));
				Animal savedAnimal = animalRepository.save(row);
				animalEmbedTriggerService.triggerIfMissing(savedAnimal);
				shelterIdentity.resolve(savedAnimal.getCareRegNo(), savedAnimal.getCareAddr())
						.map(ShelterIdentity.Values::shelterKey)
						.filter(synchronizedShelters::add)
						.ifPresent(key -> upsertShelterSafely(savedAnimal, key));
			}

			saved = saved + itemList.size();

			if (pageNo * pageSize >= totalCount) {
				break;
			}
			pageNo = pageNo + 1;
		}

		try {
			shelterGeocodingService.processBatch();
		} catch (RuntimeException exception) {
			log.warn("Shelter geocoding batch failed after animal synchronization");
		}

		return animalRepository.findAll();

	}

	private void upsertShelterSafely(Animal animal, String shelterKey) {
		try {
			shelterLocationService.upsert(animal);
		} catch (RuntimeException exception) {
			log.warn("Shelter location upsert failed for shelterKey={}", shelterKey);
		}
	}

	/** 보호소 공고 동기화. 1시간마다 실행 후 missing embed 트리거. */
	@Scheduled(fixedRate = 60 * 60 * 1000)
	public void scheduledSync() {
		syncAbandonedAnimals();
	}
}
