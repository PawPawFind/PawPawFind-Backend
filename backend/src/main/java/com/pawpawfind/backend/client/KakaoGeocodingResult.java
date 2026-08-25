package com.pawpawfind.backend.client;

public record KakaoGeocodingResult(Outcome outcome, Double latitude, Double longitude) {

	public enum Outcome {
		SUCCESS,
		NOT_FOUND,
		CONNECTION_FAILURE,
		TIMEOUT_FAILURE,
		RETRYABLE_FAILURE,
		NON_RETRYABLE_FAILURE
	}

	public static KakaoGeocodingResult success(double latitude, double longitude) {
		return new KakaoGeocodingResult(Outcome.SUCCESS, latitude, longitude);
	}

	public static KakaoGeocodingResult of(Outcome outcome) {
		return new KakaoGeocodingResult(outcome, null, null);
	}

	public boolean retryable() {
		return outcome == Outcome.CONNECTION_FAILURE || outcome == Outcome.TIMEOUT_FAILURE
				|| outcome == Outcome.RETRYABLE_FAILURE;
	}
}
