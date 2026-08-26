package com.pawpawfind.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.SocketTimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.pawpawfind.backend.dto.SearchAreaAiRequest;
import com.pawpawfind.backend.dto.SearchAreaResponse;

class SearchAreaAiClientTest {

	private MockRestServiceServer server;
	private SearchAreaAiClient client;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://ai.test");
		server = MockRestServiceServer.bindTo(builder).build();
		client = new SearchAreaAiClient(builder.build());
	}

	@Test
	void serializesRequestAndDeserializesSuccessfulResponse() {
		server.expect(once(), requestTo("http://ai.test/search-areas"))
				.andExpect(method(POST))
				.andExpect(content().json("""
						{"reportId":14,"species":"강아지"}
						""", false))
				.andRespond(withSuccess("""
						{"reportId":14,"algorithmVersion":"v1","behaviorType":"CAUTIOUS",
						 "estimatedRadiusMeters":800.0,"environmentSource":"overpass","fallbackUsed":true,
						 "assumptions":["night"],"areas":[{"rank":1,"center":{"latitude":37.5,"longitude":127.0},
						 "radiusMeters":250.0,"priorityScore":0.8,"reasonCodes":["HOME"],"reason":"주거지"}]}
						""", MediaType.APPLICATION_JSON));
		SearchAreaAiRequest request = new SearchAreaAiRequest();
		request.setReportId(14L);
		request.setSpecies("강아지");

		SearchAreaResponse response = client.recommend(request);

		assertThat(response.getAlgorithmVersion()).isEqualTo("v1");
		assertThat(response.getFallbackUsed()).isTrue();
		assertThat(response.getAreas().get(0).getCenter().getLongitude()).isEqualTo(127.0);
		server.verify();
	}

	@Test
	void serializesNormalizedKoreanSizeInRequestBody() {
		server.expect(once(), requestTo("http://ai.test/search-areas"))
				.andExpect(method(POST))
				.andExpect(content().json("""
						{"reportId":14,"size":"소형"}
						""", false))
				.andRespond(withSuccess("""
						{"reportId":14,"areas":[]}
						""", MediaType.APPLICATION_JSON));

		SearchAreaAiRequest request = request();
		request.setSize("소형");

		client.recommend(request);

		server.verify();
	}

	@Test
	void mapsAi422To422AndOther4xx5xxTo502() {
		assertStatus(withStatus(HttpStatus.UNPROCESSABLE_ENTITY), HttpStatus.UNPROCESSABLE_ENTITY);
		setUp();
		assertStatus(withStatus(HttpStatus.BAD_REQUEST), HttpStatus.BAD_GATEWAY);
		setUp();
		assertStatus(withStatus(HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.BAD_GATEWAY);
	}

	@Test
	void mapsConnectionAndTimeoutFailuresTo503() {
		assertStatus(withException(new java.net.ConnectException("refused")), HttpStatus.SERVICE_UNAVAILABLE);
		setUp();
		assertStatus(withException(new SocketTimeoutException("timed out")), HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Test
	void mapsEmptyOrMalformedResponseTo502() {
		server.expect(requestTo("http://ai.test/search-areas"))
				.andRespond(withSuccess("", MediaType.APPLICATION_JSON));
		assertResponseStatus(HttpStatus.BAD_GATEWAY);
		server.verify();

		setUp();
		server.expect(requestTo("http://ai.test/search-areas"))
				.andRespond(withSuccess("{not-json", MediaType.APPLICATION_JSON));
		assertResponseStatus(HttpStatus.BAD_GATEWAY);
		server.verify();
	}

	@Test
	void rejectsEmptyObjectAndInvalidReportIds() {
		assertJsonResponseStatus("{}", HttpStatus.BAD_GATEWAY);
		setUp();
		assertJsonResponseStatus("{\"reportId\":99,\"areas\":[]}", HttpStatus.BAD_GATEWAY);
		setUp();
		assertJsonResponseStatus("{\"reportId\":null,\"areas\":[]}", HttpStatus.BAD_GATEWAY);
	}

	@Test
	void rejectsMissingOrNullAreasButAllowsEmptyAreas() {
		assertJsonResponseStatus("{\"reportId\":14}", HttpStatus.BAD_GATEWAY);
		setUp();
		assertJsonResponseStatus("{\"reportId\":14,\"areas\":null}", HttpStatus.BAD_GATEWAY);

		setUp();
		server.expect(requestTo("http://ai.test/search-areas"))
				.andRespond(withSuccess("{\"reportId\":14,\"areas\":[]}", MediaType.APPLICATION_JSON));
		SearchAreaResponse response = client.recommend(request());
		assertThat(response.getAreas()).isEmpty();
		server.verify();
	}

	private void assertStatus(org.springframework.test.web.client.ResponseCreator responseCreator,
			HttpStatus expected) {
		server.expect(requestTo("http://ai.test/search-areas")).andRespond(responseCreator);
		assertResponseStatus(expected);
		server.verify();
	}

	private void assertResponseStatus(HttpStatus expected) {
		assertThatThrownBy(() -> client.recommend(request()))
				.isInstanceOfSatisfying(ResponseStatusException.class,
						exception -> assertThat(exception.getStatusCode()).isEqualTo(expected));
	}

	private void assertJsonResponseStatus(String json, HttpStatus expected) {
		server.expect(requestTo("http://ai.test/search-areas"))
				.andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
		assertResponseStatus(expected);
		server.verify();
	}

	private SearchAreaAiRequest request() {
		SearchAreaAiRequest request = new SearchAreaAiRequest();
		request.setReportId(14L);
		return request;
	}
}
