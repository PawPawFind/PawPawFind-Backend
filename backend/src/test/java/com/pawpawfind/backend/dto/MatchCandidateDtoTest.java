package com.pawpawfind.backend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class MatchCandidateDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void serializesShelterInCamelCaseWithoutChangingExistingFields() throws Exception {
		MatchShelterDto shelter = new MatchShelterDto();
		shelter.setCareRegNo("REG-1");
		shelter.setName("행복 보호소");
		shelter.setAddress("서울시 송파구");
		shelter.setTelephone("02-123-4567");
		shelter.setLatitude(37.5);
		shelter.setLongitude(127.1);

		MatchCandidateDto candidate = new MatchCandidateDto();
		candidate.setRank((short) 1);
		candidate.setCandidateType("SHELTER");
		candidate.setDesertionNo("A-1");
		candidate.setImageUrl("https://example.com/animal.jpg");
		candidate.setShelter(shelter);

		JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(candidate));

		assertThat(json.get("rank").asInt()).isEqualTo(1);
		assertThat(json.get("candidateType").asText()).isEqualTo("SHELTER");
		assertThat(json.get("desertionNo").asText()).isEqualTo("A-1");
		assertThat(json.get("imageUrl").asText()).isEqualTo("https://example.com/animal.jpg");
		assertThat(json.at("/shelter/careRegNo").asText()).isEqualTo("REG-1");
		assertThat(json.at("/shelter/name").asText()).isEqualTo("행복 보호소");
		assertThat(json.at("/shelter/address").asText()).isEqualTo("서울시 송파구");
		assertThat(json.at("/shelter/telephone").asText()).isEqualTo("02-123-4567");
		assertThat(json.at("/shelter/latitude").asDouble()).isEqualTo(37.5);
		assertThat(json.at("/shelter/longitude").asDouble()).isEqualTo(127.1);
	}

	@Test
	void serializesReportCandidateWithNullShelter() throws Exception {
		MatchCandidateDto candidate = new MatchCandidateDto();
		candidate.setCandidateType("REPORT");

		JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(candidate));

		assertThat(json.has("shelter")).isTrue();
		assertThat(json.get("shelter").isNull()).isTrue();
	}
}
