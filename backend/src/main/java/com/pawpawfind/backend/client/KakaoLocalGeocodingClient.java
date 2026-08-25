package com.pawpawfind.backend.client;

import java.net.SocketTimeoutException;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.pawpawfind.backend.client.KakaoGeocodingResult.Outcome;

@Component
public class KakaoLocalGeocodingClient {

	private final RestClient restClient;

	public KakaoLocalGeocodingClient(@Qualifier("kakaoLocalRestClient") RestClient restClient) {
		this.restClient = restClient;
	}

	public KakaoGeocodingResult geocode(String address) {
		try {
			KakaoAddressResponse response = restClient.get()
					.uri(builder -> builder.path("/v2/local/search/address.json")
							.queryParam("query", address).build())
					.retrieve()
					.body(KakaoAddressResponse.class);
			if (response == null || response.getDocuments() == null) {
				return KakaoGeocodingResult.of(Outcome.RETRYABLE_FAILURE);
			}
			if (response.getDocuments().isEmpty()) {
				return KakaoGeocodingResult.of(Outcome.NOT_FOUND);
			}
			for (Document document : response.getDocuments()) {
				KakaoGeocodingResult valid = validCoordinates(document);
				if (valid != null) return valid;
			}
			return KakaoGeocodingResult.of(Outcome.NON_RETRYABLE_FAILURE);
		} catch (RestClientResponseException exception) {
			int status = exception.getStatusCode().value();
			return KakaoGeocodingResult.of(status == 429 || status >= 500
					? Outcome.RETRYABLE_FAILURE : Outcome.NON_RETRYABLE_FAILURE);
		} catch (ResourceAccessException exception) {
			return KakaoGeocodingResult.of(hasTimeoutCause(exception)
					? Outcome.TIMEOUT_FAILURE : Outcome.CONNECTION_FAILURE);
		} catch (RestClientException exception) {
			return KakaoGeocodingResult.of(Outcome.RETRYABLE_FAILURE);
		}
	}

	private KakaoGeocodingResult validCoordinates(Document document) {
		if (document == null) return null;
		try {
			double longitude = Double.parseDouble(document.getX());
			double latitude = Double.parseDouble(document.getY());
			if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
					|| latitude < -90.0 || latitude > 90.0
					|| longitude < -180.0 || longitude > 180.0) return null;
			return KakaoGeocodingResult.success(latitude, longitude);
		} catch (RuntimeException exception) {
			return null;
		}
	}

	private boolean hasTimeoutCause(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			if (current instanceof SocketTimeoutException) return true;
			current = current.getCause();
		}
		return false;
	}

	public static class KakaoAddressResponse {
		private List<Document> documents;
		public List<Document> getDocuments() { return documents; }
		public void setDocuments(List<Document> documents) { this.documents = documents; }
	}

	public static class Document {
		private String x;
		private String y;
		public String getX() { return x; }
		public void setX(String x) { this.x = x; }
		public String getY() { return y; }
		public void setY(String y) { this.y = y; }
	}
}
