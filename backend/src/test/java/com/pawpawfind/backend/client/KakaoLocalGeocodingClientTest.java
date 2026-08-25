package com.pawpawfind.backend.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.pawpawfind.backend.client.KakaoGeocodingResult.Outcome;

class KakaoLocalGeocodingClientTest {

	private MockRestServiceServer server;
	private KakaoLocalGeocodingClient client;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://kakao.test")
				.defaultHeader("Authorization", "KakaoAK secret-key");
		server = MockRestServiceServer.bindTo(builder).build();
		client = new KakaoLocalGeocodingClient(builder.build());
	}

	@Test
	void sendsAuthorizationAndEncodedQueryAndMapsXY() {
		server.expect(request -> {
			assertThat(request.getURI().getPath()).isEqualTo("/v2/local/search/address.json");
			assertThat(java.net.URLDecoder.decode(request.getURI().getRawQuery(), StandardCharsets.UTF_8))
					.isEqualTo("query=서울시 중구 & 1");
		}).andExpect(method(GET)).andExpect(header("Authorization", "KakaoAK secret-key"))
				.andRespond(withSuccess("{\"documents\":[{\"x\":\"127.1\",\"y\":\"37.5\"}]}",
						MediaType.APPLICATION_JSON));

		KakaoGeocodingResult result = client.geocode("서울시 중구 & 1");

		assertThat(result.outcome()).isEqualTo(Outcome.SUCCESS);
		assertThat(result.longitude()).isEqualTo(127.1);
		assertThat(result.latitude()).isEqualTo(37.5);
		server.verify();
	}

	@Test
	void emptyDocumentsIsNotFoundAndNullOrMalformedBodyIsRetryable() {
		assertJson("{\"documents\":[]}", Outcome.NOT_FOUND);
		setUp();
		assertJson("", Outcome.RETRYABLE_FAILURE);
		setUp();
		assertJson("{bad-json", Outcome.RETRYABLE_FAILURE);
	}

	@Test
	void invalidCoordinatesAreNonRetryableFailure() {
		assertCoordinate("NaN", "37.5");
		setUp();
		assertCoordinate("127.1", "Infinity");
		setUp();
		assertCoordinate("181", "37.5");
		setUp();
		assertCoordinate("127.1", "91");
	}

	@Test
	void connectionAndTimeoutHaveDistinctOutcomes() {
		server.expect(request -> {}).andRespond(withException(new java.net.ConnectException("secret detail")));
		assertThat(client.geocode("주소").outcome()).isEqualTo(Outcome.CONNECTION_FAILURE);
		server.verify();
		setUp();
		server.expect(request -> {}).andRespond(withException(new SocketTimeoutException("secret detail")));
		assertThat(client.geocode("주소").outcome()).isEqualTo(Outcome.TIMEOUT_FAILURE);
		server.verify();
	}

	@Test
	void maps429And5xxAsRetryableAndOther4xxAsNonRetryable() {
		assertHttp(HttpStatus.TOO_MANY_REQUESTS, Outcome.RETRYABLE_FAILURE);
		setUp();
		assertHttp(HttpStatus.INTERNAL_SERVER_ERROR, Outcome.RETRYABLE_FAILURE);
		setUp();
		assertHttp(HttpStatus.BAD_REQUEST, Outcome.NON_RETRYABLE_FAILURE);
	}

	private void assertCoordinate(String x, String y) {
		assertJson("{\"documents\":[{\"x\":\"" + x + "\",\"y\":\"" + y + "\"}]}",
				Outcome.NON_RETRYABLE_FAILURE);
	}

	private void assertJson(String json, Outcome expected) {
		server.expect(request -> {}).andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
		assertThat(client.geocode("주소").outcome()).isEqualTo(expected);
		server.verify();
	}

	private void assertHttp(HttpStatus status, Outcome expected) {
		server.expect(request -> {}).andRespond(withStatus(status));
		assertThat(client.geocode("주소").outcome()).isEqualTo(expected);
		server.verify();
	}
}
