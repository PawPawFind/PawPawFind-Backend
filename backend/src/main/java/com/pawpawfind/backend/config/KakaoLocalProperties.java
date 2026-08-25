package com.pawpawfind.backend.config;

import java.net.URI;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao.local")
public class KakaoLocalProperties implements InitializingBean {

	private boolean enabled;
	private String baseUrl = "https://dapi.kakao.com";
	private int connectTimeoutMillis = 2000;
	private int readTimeoutMillis = 5000;
	private int batchSize = 50;
	private int maxAttempts = 3;

	@Override
	public void afterPropertiesSet() {
		URI uri;
		try {
			uri = URI.create(baseUrl == null ? "" : baseUrl.trim());
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("kakao.local.base-url must be a valid HTTP URL");
		}
		if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
				|| uri.getHost() == null) {
			throw new IllegalStateException("kakao.local.base-url must be a valid HTTP URL");
		}
		baseUrl = baseUrl.trim().replaceAll("/+$", "");
		if (connectTimeoutMillis <= 0) {
			throw new IllegalStateException("kakao.local.connect-timeout-millis must be positive");
		}
		if (readTimeoutMillis <= 0) {
			throw new IllegalStateException("kakao.local.read-timeout-millis must be positive");
		}
		if (batchSize <= 0 || batchSize > 500) {
			throw new IllegalStateException("kakao.local.batch-size must be between 1 and 500");
		}
		if (maxAttempts <= 0 || maxAttempts > 10) {
			throw new IllegalStateException("kakao.local.max-attempts must be between 1 and 10");
		}
	}

	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	public String getBaseUrl() { return baseUrl; }
	public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
	public int getConnectTimeoutMillis() { return connectTimeoutMillis; }
	public void setConnectTimeoutMillis(int connectTimeoutMillis) { this.connectTimeoutMillis = connectTimeoutMillis; }
	public int getReadTimeoutMillis() { return readTimeoutMillis; }
	public void setReadTimeoutMillis(int readTimeoutMillis) { this.readTimeoutMillis = readTimeoutMillis; }
	public int getBatchSize() { return batchSize; }
	public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
	public int getMaxAttempts() { return maxAttempts; }
	public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
}
