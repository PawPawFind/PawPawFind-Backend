package com.pawpawfind.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawpawfind.backend.config.JwtAuthFilter;
import com.pawpawfind.backend.dto.SearchAreaCompareResponse;
import com.pawpawfind.backend.service.SearchAreaService;
import java.nio.charset.StandardCharsets;

class SearchAreaCompareControllerTest {
    private final SearchAreaService service = mock(SearchAreaService.class);
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void bodylessRequestReturnsCamelCaseFullResponseAndPreservesOffset() throws Exception {
        try (var input = getClass().getResourceAsStream("/fixtures/search-area-compare-response.json")) {
            var response = mapper.readValue(input, SearchAreaCompareResponse.class);
            when(service.compare(1L, 7L, "USER")).thenReturn(response);
            var mvc = MockMvcBuilders.standaloneSetup(new SearchAreaController(service)).build();
            String json = mvc.perform(post("/api/reports/1/search-areas/compare")
                    .requestAttr(JwtAuthFilter.USER_ID_ATTR, 7L).requestAttr(JwtAuthFilter.ROLE_ATTR, "USER"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.comparedAt").value("2026-10-29T12:00:00+09:00"))
                    .andExpect(jsonPath("$.baseline.algorithmVersion").value("HEURISTIC_V1"))
                    .andExpect(jsonPath("$.improved.areas[0].scoreBreakdown.behaviorEnvironment").value(23))
                    .andExpect(jsonPath("$.comparison.rankChanges[1].centerShiftMeters").value(400.08))
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
            assertThat(mapper.readTree(json)).isEqualTo(mapper.readTree(mapper.writeValueAsString(response)));
            verify(service).compare(1L, 7L, "USER");
        }
    }

    @Test
    void fallbackReturnsExplicitNullScoreBreakdown() throws Exception {
        try (var input = getClass().getResourceAsStream("/fixtures/search-area-compare-response.json")) {
            var response = mapper.readValue(input, SearchAreaCompareResponse.class);
            response.getBaseline().setFallbackUsed(true);
            response.getBaseline().getAreas().forEach(area -> area.setScoreBreakdown(null));
            when(service.compare(1L, 7L, "USER")).thenReturn(response);
            var mvc = MockMvcBuilders.standaloneSetup(new SearchAreaController(service)).build();
            String json = mvc.perform(post("/api/reports/1/search-areas/compare")
                    .requestAttr(JwtAuthFilter.USER_ID_ATTR, 7L).requestAttr(JwtAuthFilter.ROLE_ATTR, "USER"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            var breakdown = mapper.readTree(json).at("/baseline/areas/0/scoreBreakdown");
            assertThat(breakdown.isMissingNode()).isFalse();
            assertThat(breakdown.isNull()).isTrue();
        }
    }

    @Test
    void forwardsAllPolicyErrors() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new SearchAreaController(service)).build();
        for (var status : new HttpStatus[]{HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN, HttpStatus.NOT_FOUND,
                HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.BAD_GATEWAY, HttpStatus.SERVICE_UNAVAILABLE}) {
            doThrow(new ResponseStatusException(status)).when(service).compare(1L, null, null);
            mvc.perform(post("/api/reports/1/search-areas/compare")).andExpect(status().is(status.value()));
        }
    }
}
