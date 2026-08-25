package com.pawpawfind.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * FE(로컬 Vite / 운영 도메인 / S3 정적 페이지)에서 BE API 호출용 CORS.
 * 운영 사이트는 HTTP·HTTPS 모두 허용한다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins(
						"http://localhost:5173",
						"http://127.0.0.1:5173",
						"https://localhost:5173",
						"https://127.0.0.1:5173",
						"http://www.pawpawfind.com",
						"https://www.pawpawfind.com",
						"http://pawpawfind.com",
						"https://pawpawfind.com",
						"http://3.38.54.145",
						"http://pawpawfind-dev-web.s3-website-ap-southeast-2.amazonaws.com")
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("Authorization", "Content-Type")
				.exposedHeaders("Authorization");
	}
}
