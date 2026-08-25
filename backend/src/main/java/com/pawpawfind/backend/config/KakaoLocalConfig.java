package com.pawpawfind.backend.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(KakaoLocalProperties.class)
public class KakaoLocalConfig {

	@Bean
	@Qualifier("kakaoLocalRestClient")
	RestClient kakaoLocalRestClient(KakaoLocalProperties properties,
			@Value("${kakao.rest-api-key:}") String restApiKey) {
		if (properties.isEnabled() && (restApiKey == null || restApiKey.isBlank())) {
			throw new IllegalStateException("kakao.rest-api-key must not be blank when Kakao Local API is enabled");
		}
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMillis()));
		requestFactory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMillis()));
		return RestClient.builder()
				.baseUrl(properties.getBaseUrl())
				.requestFactory(requestFactory)
				.defaultHeader("Authorization", "KakaoAK " + (restApiKey == null ? "" : restApiKey.trim()))
				.build();
	}
}
