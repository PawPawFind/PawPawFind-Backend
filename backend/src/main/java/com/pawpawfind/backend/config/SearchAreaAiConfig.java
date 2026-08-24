package com.pawpawfind.backend.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(SearchAreaAiProperties.class)
public class SearchAreaAiConfig {

	@Bean
	@Qualifier("searchAreaRestClient")
	RestClient searchAreaRestClient(SearchAreaAiProperties properties) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMillis()));
		requestFactory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMillis()));
		return RestClient.builder()
				.baseUrl(properties.getUrl())
				.requestFactory(requestFactory)
				.build();
	}
}
