package com.pawpawfind.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 포포파인드 백엔드 진입점.
 * 클래스명 Beckend는 프로젝트 생성 시 오타이며, 패키지/실행 설정과 맞춰 유지한다.
 */
@SpringBootApplication
public class BeckendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeckendApplication.class, args);
	}

}
