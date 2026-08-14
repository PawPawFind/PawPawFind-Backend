package com.pawpawfind.backend.service;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
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
	private final AnimalRepository animalRepository;

	public AbandonedService(AnimalRepository animalRepository) {
		this.animalRepository = animalRepository;
	}

	@Value("${animal.api.key}")
	private String apiKey;

	@Value("${animal.api.url}")
	private String apiUrl;

	private final RestClient restClient = RestClient.create();

	public Object syncAbandonedAnimals() {
		int pageNo = 1;
		int pageSize = 1000;
		int totalCount = 0;
		int saved = 0;

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
				animalRepository.save(row);
			}

			saved = saved + itemList.size();

			if (pageNo * pageSize >= totalCount) {
				break;
			}
			pageNo = pageNo + 1;
		}

        return animalRepository.findAll();

	}
}
