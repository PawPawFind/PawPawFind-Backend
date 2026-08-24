package com.pawpawfind.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

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
}
