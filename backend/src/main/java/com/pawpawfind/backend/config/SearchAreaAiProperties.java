package com.pawpawfind.backend.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.beans.factory.InitializingBean;

@ConfigurationProperties(prefix = "ai.service")
public class SearchAreaAiProperties implements InitializingBean {

	private String url;
	private int connectTimeoutMillis = 2000;
	private int readTimeoutMillis = 15000;

	@Override
	public void afterPropertiesSet() {
		if (url == null || url.isBlank()) {
			throw new IllegalStateException("ai.service.url must not be blank");
		}
		URI uri;
		try {
			uri = URI.create(url.trim());
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("ai.service.url must be a valid HTTP URL");
		}
		if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
				|| uri.getHost() == null) {
			throw new IllegalStateException("ai.service.url must be a valid HTTP URL");
		}
		url = url.trim().replaceAll("/+$", "");
		if (connectTimeoutMillis <= 0) {
			throw new IllegalStateException("ai.service.connect-timeout-millis must be positive");
		}
		if (readTimeoutMillis <= 0) {
			throw new IllegalStateException("ai.service.read-timeout-millis must be positive");
		}
	}

	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }
	public int getConnectTimeoutMillis() { return connectTimeoutMillis; }
	public void setConnectTimeoutMillis(int connectTimeoutMillis) { this.connectTimeoutMillis = connectTimeoutMillis; }
	public int getReadTimeoutMillis() { return readTimeoutMillis; }
	public void setReadTimeoutMillis(int readTimeoutMillis) { this.readTimeoutMillis = readTimeoutMillis; }
}
