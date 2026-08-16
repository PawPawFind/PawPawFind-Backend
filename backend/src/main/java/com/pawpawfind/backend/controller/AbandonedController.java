package com.pawpawfind.backend;

import com.pawpawfind.backend.service.AbandonedService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 공공 유기동물 공고 API.
 * GET /abandoned 호출 시 공고중(notice) 전건을 받아 DB에 저장한 뒤 목록을 반환한다.
 * 사용자 요청마다 공공 API를 치므로, 이후에는 스케줄 동기화 + DB 조회로 분리할 것.
 */
@RestController
public class AbandonedController {

	private final AbandonedService abandonedService;


	public AbandonedController(AbandonedService abandonedService){
		this.abandonedService = abandonedService;
	}

	@GetMapping("/abandoned")
	public Object getAbandonedAnimals(){
		return abandonedService.syncAbandonedAnimals();
	}


}
