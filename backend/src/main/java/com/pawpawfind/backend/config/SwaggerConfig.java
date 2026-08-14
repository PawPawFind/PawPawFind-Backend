package com.pawpawfind.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Swagger UI 문서 제목/설명.
 * 화면: http://127.0.0.1:8080/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("PawPawFind API")
						.version("1.0.0")
						.description("유기동물 공고 동기화, 실종/발견 제보"));
	}
}
