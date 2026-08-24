package com.pawpawfind.backend.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.pawpawfind.backend.dto.SearchAreaAiRequest;
import com.pawpawfind.backend.dto.SearchAreaResponse;

/** AI POST /search-areas 호출과 외부 오류 변환만 담당한다. */
@Component
public class SearchAreaAiClient {

	private final RestClient restClient;

	public SearchAreaAiClient(@Qualifier("searchAreaRestClient") RestClient restClient) {
		this.restClient = restClient;
	}

	public SearchAreaResponse recommend(SearchAreaAiRequest request) {
		try {
			SearchAreaResponse response = restClient.post()
					.uri("/search-areas")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.body(request)
					.retrieve()
					.body(SearchAreaResponse.class);
			if (response == null) {
				throw badGateway();
			}
			return response;
		} catch (RestClientResponseException exception) {
			if (exception.getStatusCode().value() == 422) {
				throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
						"AI가 추천 요청을 처리할 수 없습니다.");
			}
			throw badGateway();
		} catch (ResourceAccessException exception) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"추천 서비스에 연결할 수 없습니다.");
		} catch (RestClientException exception) {
			throw badGateway();
		}
	}

	private ResponseStatusException badGateway() {
		return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
				"추천 서비스가 올바른 응답을 반환하지 않았습니다.");
	}
}
