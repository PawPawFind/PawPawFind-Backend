package com.pawpawfind.backend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

class SearchAreaDtoTest {

	@Test
	void responseUsesCamelCaseJsonContract() throws Exception {
		SearchAreaCenterResponse center = new SearchAreaCenterResponse();
		center.setLatitude(37.5);
		center.setLongitude(127.0);
		SearchAreaItemResponse area = new SearchAreaItemResponse();
		area.setRank(1);
		area.setCenter(center);
		area.setRadiusMeters(250.0);
		area.setPriorityScore(0.8);
		area.setReasonCodes(List.of("HOME_BIAS"));
		area.setReason("주거지 우선");
		SearchAreaResponse response = new SearchAreaResponse();
		response.setReportId(14L);
		response.setAlgorithmVersion("v1");
		response.setBehaviorType("CAUTIOUS");
		response.setEstimatedRadiusMeters(800.0);
		response.setEnvironmentSource("overpass");
		response.setFallbackUsed(true);
		response.setAssumptions(List.of("야간 이동"));
		response.setAreas(List.of(area));

		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(response);
		assertThat(json).contains("\"algorithmVersion\"", "\"estimatedRadiusMeters\"",
				"\"fallbackUsed\"", "\"radiusMeters\"", "\"priorityScore\"",
				"\"reasonCodes\"");
		assertThat(json).doesNotContain("algorithm_version", "radius_meters");
		SearchAreaResponse decoded = mapper.readValue(json, SearchAreaResponse.class);
		assertThat(decoded.getAreas().get(0).getCenter().getLatitude()).isEqualTo(37.5);
	}
}
