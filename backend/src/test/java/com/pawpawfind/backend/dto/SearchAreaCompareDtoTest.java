package com.pawpawfind.backend.dto;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

class SearchAreaCompareDtoTest {
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void roundTripsFullActualAiFixtureIncludingOffsetAndReasonArrays() throws Exception {
        try (var input = getClass().getResourceAsStream("/fixtures/search-area-compare-response.json")) {
            var tree = mapper.readTree(input);
            var response = mapper.treeToValue(tree, SearchAreaCompareResponse.class);
            assertThat(response.getComparedAt().getOffset()).isEqualTo(ZoneOffset.ofHours(9));
            assertThat(mapper.readTree(mapper.writeValueAsString(response))).isEqualTo(tree);
            assertThat(response.getBaseline().getAreas().get(0).getPriorityScore()).isEqualTo(92);
            assertThat(response.getImproved().getAreas().get(0).getScoreBreakdown().getBehaviorEnvironment()).isEqualTo(23);
        }
    }

    @Test
    void nullFieldsSurviveGlobalNonNullPolicyAndAddedRemovedChangesRoundTrip() throws Exception {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        var area = new SearchAreaExplainedItemResponse();
        assertThat(mapper.valueToTree(area).get("scoreBreakdown").isNull()).isTrue();
        for (String status : new String[]{"ADDED", "REMOVED"}) {
            var change = new SearchAreaRankChangeResponse();
            change.setRank(3); change.setStatus(status);
            var tree = mapper.valueToTree(change);
            for (String field : new String[]{"beforeCenter", "afterCenter", "beforeRadiusMeters", "afterRadiusMeters",
                    "beforePriorityScore", "afterPriorityScore", "centerShiftMeters", "scoreDelta"}) {
                assertThat(tree.has(field)).isTrue();
                assertThat(tree.get(field).isNull()).isTrue();
            }
            var decoded = mapper.treeToValue(tree, SearchAreaRankChangeResponse.class);
            assertThat(decoded.getStatus()).isEqualTo(status);
            assertThat(decoded.getCenterShiftMeters()).isNull();
            assertThat(decoded.getScoreDelta()).isNull();
        }
    }

    @Test
    void existingV1FieldsRetainDoubleTypesAndNeverGainBreakdown() throws Exception {
        var area = new SearchAreaItemResponse();
        area.setPriorityScore(0.8); area.setRadiusMeters(250.5);
        var tree = mapper.valueToTree(area);
        assertThat(tree.has("scoreBreakdown")).isFalse();
        assertThat(tree.get("priorityScore").doubleValue()).isEqualTo(0.8);
        assertThat(mapper.treeToValue(tree, SearchAreaItemResponse.class).getRadiusMeters()).isEqualTo(250.5);
    }
}
