package com.pawpawfind.backend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

class SearchAreaAiPropertiesTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(SearchAreaAiConfig.class);

	@Test
	void appliesConfiguredUrlAndTimeouts() {
		contextRunner.withPropertyValues(
				"ai.service.url=http://localhost:9123/base/",
				"ai.service.connect-timeout-millis=1234",
				"ai.service.read-timeout-millis=16000")
				.run(context -> {
					assertThat(context).hasNotFailed();
					SearchAreaAiProperties properties = context.getBean(SearchAreaAiProperties.class);
					assertThat(properties.getUrl()).isEqualTo("http://localhost:9123/base");
					assertThat(properties.getConnectTimeoutMillis()).isEqualTo(1234);
					assertThat(properties.getReadTimeoutMillis()).isEqualTo(16000);
					assertThat(context).hasSingleBean(RestClient.class);
				});
	}

	@Test
	void rejectsBlankUrlAndNonPositiveTimeouts() {
		contextRunner.withPropertyValues("ai.service.url= ")
				.run(context -> assertThat(context).hasFailed());
		contextRunner.withPropertyValues(
				"ai.service.url=http://localhost", "ai.service.connect-timeout-millis=0")
				.run(context -> assertThat(context).hasFailed());
		contextRunner.withPropertyValues(
				"ai.service.url=http://localhost", "ai.service.read-timeout-millis=-1")
				.run(context -> assertThat(context).hasFailed());
	}

	@Test
	void rejectsHttpUrlsWithoutHost() {
		assertInvalidUrl("http:/search-areas");
		assertInvalidUrl("http:search-areas");
	}

	@Test
	void acceptsHttpAndHttpsUrlsWithHost() {
		assertValidUrl("  http://localhost:9123/base/  ", "http://localhost:9123/base");
		assertValidUrl("https://ai.example.com/", "https://ai.example.com");
	}

	private void assertInvalidUrl(String url) {
		SearchAreaAiProperties properties = new SearchAreaAiProperties();
		properties.setUrl(url);
		assertThatThrownBy(properties::afterPropertiesSet)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("ai.service.url must be a valid HTTP URL");
	}

	private void assertValidUrl(String url, String expected) {
		SearchAreaAiProperties properties = new SearchAreaAiProperties();
		properties.setUrl(url);
		properties.afterPropertiesSet();
		assertThat(properties.getUrl()).isEqualTo(expected);
	}
}
