package com.pawpawfind.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Swagger UI 문서.
 * 화면: http://127.0.0.1:8080/swagger-ui.html
 * Authorize에 JWT를 한 번 넣으면 Bearer 헤더가 요청에 붙는다.
 */
@Configuration
public class SwaggerConfig {

	private static final String BEARER_SCHEME = "bearerAuth";

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("PawPawFind API")
						.version("1.0.0")
						.description("""
								유기동물 공고 동기화, 실종/발견 제보.

								인증이 필요한 API는 우측 상단 **Authorize**에
								카카오 로그인 후 받은 JWT를 넣으면 됩니다.
								(Bearer 접두사 없이 토큰만 입력)
								"""))
				.components(new Components()
						.addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
								.name(BEARER_SCHEME)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("카카오 로그인 응답의 token 값")))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
	}
}
