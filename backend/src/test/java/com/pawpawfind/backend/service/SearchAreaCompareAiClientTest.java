package com.pawpawfind.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.nio.charset.StandardCharsets;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.*;
import org.springframework.test.web.client.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pawpawfind.backend.dto.*;

class SearchAreaCompareAiClientTest {
    private MockRestServiceServer server;
    private SearchAreaAiClient client;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ai.test");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new SearchAreaAiClient(builder.build());
    }

    public static String fixture(String name) throws Exception {
        try (var stream = SearchAreaCompareAiClientTest.class.getResourceAsStream(
                "/fixtures/search-area-compare-" + name + ".json")) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private SearchAreaAiRequest request() throws Exception {
        return mapper.readValue(fixture("request"), SearchAreaAiRequest.class);
    }

    @Test
    void sendsExactlyOneCompareRequestAndDecodesEntireAiFixtureWithOffset() throws Exception {
        server.expect(ExpectedCount.once(), requestTo("http://ai.test/search-areas/compare"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Accept", "application/json"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(fixture("request"), true))
                .andRespond(withSuccess(fixture("response"), MediaType.APPLICATION_JSON));
        var response = client.compare(request());
        assertThat(response.getComparedAt().getOffset()).isEqualTo(ZoneOffset.ofHours(9));
        assertThat(mapper.readTree(mapper.writeValueAsString(response))).isEqualTo(mapper.readTree(fixture("response")));
        server.verify();
    }

    static Stream<String> invalidResponses() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        java.util.List<String> cases = new java.util.ArrayList<>();
        cases.add(""); cases.add("{bad-json"); cases.add("{}"); cases.add("null");
        for (String field : new String[]{"reportId", "comparedAt", "baseline", "improved", "comparison"}) {
            ObjectNode root = (ObjectNode) mapper.readTree(fixture("response"));
            root.remove(field); cases.add(root.toString());
        }
        ObjectNode mismatch = (ObjectNode) mapper.readTree(fixture("response"));
        mismatch.put("reportId", 99); cases.add(mismatch.toString());
        for (String side : new String[]{"baseline", "improved"}) {
            for (String field : new String[]{"algorithmVersion", "areas", "fallbackUsed"}) {
                ObjectNode root = (ObjectNode) mapper.readTree(fixture("response"));
                ((ObjectNode) root.get(side)).remove(field); cases.add(root.toString());
            }
            ObjectNode root = (ObjectNode) mapper.readTree(fixture("response"));
            ((ObjectNode) root.get(side)).put("algorithmVersion", "UNKNOWN"); cases.add(root.toString());
        }
        for (String field : new String[]{"rank", "center", "radiusMeters", "priorityScore", "reasonCodes", "reason", "scoreBreakdown"}) {
            ObjectNode root = (ObjectNode) mapper.readTree(fixture("response"));
            ((ObjectNode) root.at("/improved/areas/0")).remove(field); cases.add(root.toString());
        }
        for (String field : new String[]{"rankChanges", "areaCountBefore", "areaCountAfter"}) {
            ObjectNode root = (ObjectNode) mapper.readTree(fixture("response"));
            ((ObjectNode) root.get("comparison")).remove(field); cases.add(root.toString());
        }
        for (String field : new String[]{"latitude", "longitude"}) {
            ObjectNode root = (ObjectNode) mapper.readTree(fixture("response"));
            ((ObjectNode) root.at("/baseline/areas/0/center")).remove(field);
            cases.add(root.toString());
        }
        for (String field : new String[]{"distance", "behaviorEnvironment", "corridor", "accessibility", "eventContext"}) {
            ObjectNode root = (ObjectNode) mapper.readTree(fixture("response"));
            ((ObjectNode) root.at("/baseline/areas/0/scoreBreakdown")).remove(field);
            cases.add(root.toString());
        }
        for (int value : new int[]{-1, 29, 31}) {
            ObjectNode root = (ObjectNode) mapper.readTree(fixture("response"));
            ((ObjectNode) root.at("/baseline/areas/0/scoreBreakdown")).put("distance", value);
            cases.add(root.toString());
        }
        return cases.stream();
    }

    @ParameterizedTest
    @MethodSource("invalidResponses")
    void rejectsInvalidContract(String json) throws Exception {
        assertFailure(withSuccess(json, MediaType.APPLICATION_JSON), 502);
    }

    @Test
    void translatesHttpAndNetworkErrorsWithoutInternalDetails() throws Exception {
        for (HttpStatus status : new HttpStatus[]{HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.BAD_REQUEST,
                HttpStatus.UNAUTHORIZED, HttpStatus.NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.SERVICE_UNAVAILABLE}) {
            setUp();
            assertFailure(withStatus(status).body("private AI details"), status.value() == 422 ? 422 : 502);
        }
        setUp(); assertFailure(withException(new ConnectException("private endpoint")), 503);
        setUp(); assertFailure(withException(new SocketTimeoutException("private endpoint")), 503);
    }

    @Test
    void fallbackIsSuccessfulAndKeepsNullBreakdowns() throws Exception {
        ObjectNode root = (ObjectNode) mapper.readTree(fixture("response"));
        for (String side : new String[]{"baseline", "improved"}) {
            ((ObjectNode) root.get(side)).put("fallbackUsed", true);
            for (var area : root.at("/" + side + "/areas")) ((ObjectNode) area).putNull("scoreBreakdown");
        }
        ((ObjectNode) root.get("comparison")).put("fallbackUsed", true);
        server.expect(requestTo("http://ai.test/search-areas/compare"))
                .andRespond(withSuccess(root.toString(), MediaType.APPLICATION_JSON));
        assertThat(client.compare(request()).getBaseline().getAreas().get(0).getScoreBreakdown()).isNull();
        server.verify();
    }

    @Test
    void addedAndRemovedRanksPreserveNullableFieldsAndAiValues() throws Exception {
        ObjectNode root = (ObjectNode) mapper.readTree(fixture("response"));
        for (int index = 0; index < 2; index++) {
            ObjectNode change = (ObjectNode) root.at("/comparison/rankChanges/" + index);
            change.put("status", index == 0 ? "ADDED" : "REMOVED");
            String side = index == 0 ? "before" : "after";
            change.putNull(side + "Center");
            change.putNull(side + "RadiusMeters");
            change.putNull(side + "PriorityScore");
            change.putNull("centerShiftMeters"); change.putNull("scoreDelta");
        }
        server.expect(requestTo("http://ai.test/search-areas/compare"))
                .andRespond(withSuccess(root.toString(), MediaType.APPLICATION_JSON));
        var changes = client.compare(request()).getComparison().getRankChanges();
        assertThat(changes.get(0).getBeforeCenter()).isNull();
        assertThat(changes.get(1).getAfterCenter()).isNull();
        for (var change : changes) {
            assertThat(change.getCenterShiftMeters()).isNull();
            assertThat(change.getScoreDelta()).isNull();
        }
        server.verify();
    }

    private void assertFailure(ResponseCreator creator, int status) throws Exception {
        server.expect(requestTo("http://ai.test/search-areas/compare")).andRespond(creator);
        assertThatThrownBy(() -> client.compare(request())).isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
            assertThat(ex.getStatusCode().value()).isEqualTo(status);
            assertThat(ex.getReason()).doesNotContain("private", "ai.test");
        });
        server.verify();
    }
}
