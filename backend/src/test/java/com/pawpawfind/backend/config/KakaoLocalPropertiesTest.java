package com.pawpawfind.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class KakaoLocalPropertiesTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withUserConfiguration(KakaoLocalConfig.class);

	@Test
	void disabledByDefaultAndAcceptsValidEnabledConfiguration() {
		runner.withPropertyValues("kakao.rest-api-key=")
				.run(context -> {
					assertThat(context).hasNotFailed();
					assertThat(context.getBean(KakaoLocalProperties.class).isEnabled()).isFalse();
				});
		runner.withPropertyValues(
				"kakao.rest-api-key=test-key", "kakao.local.enabled=true",
				"kakao.local.base-url=https://dapi.kakao.com/",
				"kakao.local.connect-timeout-millis=1000",
				"kakao.local.read-timeout-millis=4000",
				"kakao.local.batch-size=20", "kakao.local.max-attempts=2")
				.run(context -> {
					assertThat(context).hasNotFailed();
					KakaoLocalProperties properties = context.getBean(KakaoLocalProperties.class);
					assertThat(properties.getBaseUrl()).isEqualTo("https://dapi.kakao.com");
					assertThat(properties.getConnectTimeoutMillis()).isEqualTo(1000);
					assertThat(properties.getReadTimeoutMillis()).isEqualTo(4000);
				});
	}

	@Test
	void rejectsMissingKeyHostAndInvalidLimits() {
		assertFailed("kakao.local.enabled=true", "kakao.rest-api-key= ");
		assertFailed("kakao.local.base-url=http:local", "kakao.rest-api-key=test");
		assertFailed("kakao.local.connect-timeout-millis=0", "kakao.rest-api-key=test");
		assertFailed("kakao.local.read-timeout-millis=-1", "kakao.rest-api-key=test");
		assertFailed("kakao.local.batch-size=0", "kakao.rest-api-key=test");
		assertFailed("kakao.local.batch-size=501", "kakao.rest-api-key=test");
		assertFailed("kakao.local.max-attempts=0", "kakao.rest-api-key=test");
		assertFailed("kakao.local.max-attempts=11", "kakao.rest-api-key=test");
	}

	private void assertFailed(String... properties) {
		runner.withPropertyValues(properties).run(context -> assertThat(context).hasFailed());
	}
}
