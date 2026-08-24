package com.pawpawfind.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pawpawfind.backend.service.AbandonedService;

/**
 * 공공 유기동물 공고 수동 동기화.
 * GET /abandoned — 공고중(notice) 전건을 받아 DB에 저장한 뒤 목록을 반환한다.
 * 일상 조회는 GET /api/animals 를 쓴다. 주기 동기화는 AbandonedService.@Scheduled.
 */
@RestController
public class AbandonedController {

	private final AbandonedService abandonedService;

	public AbandonedController(AbandonedService abandonedService) {
		this.abandonedService = abandonedService;
	}

	@GetMapping("/abandoned")
	public Object getAbandonedAnimals() {
		return abandonedService.syncAbandonedAnimals();
	}
}
