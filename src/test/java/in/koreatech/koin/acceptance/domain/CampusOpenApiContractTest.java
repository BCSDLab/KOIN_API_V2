package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.support.JsonAssertions;

class CampusOpenApiContractTest extends AcceptanceTest {

    private static final String CAMPUS_GROUP = "3. Campus API";

    @Test
    void root_list_success_schema_is_preserved() throws Exception {
        JsonNode openApi = campusOpenApi();
        JsonNode response = openApi.at("/paths/~1callvan~1notifications/get/responses/200");

        assertThat(response.isMissingNode()).isFalse();
        JsonNode schema = findResponseSchema(response);
        assertThat(schema.path("type").asText()).isEqualTo("array");
        assertThat(schema.path("items").path("$ref").asText()).endsWith("CallvanNotificationResponse");
    }

    @Test
    void explicit_created_and_no_content_responses_do_not_keep_phantom_200() throws Exception {
        JsonNode openApi = campusOpenApi();
        JsonNode created = openApi.at("/paths/~1callvan/post/responses");
        JsonNode noContent = openApi.at(
            "/paths/~1callvan~1notifications~1mark-all-read/post/responses");

        assertThat(created.has("200")).isFalse();
        assertThat(created.has("201")).isTrue();
        assertThat(findResponseSchema(created.path("201")).path("$ref").asText())
            .endsWith("CallvanPostCreateResponse");
        assertThat(noContent.has("200")).isFalse();
        assertThat(noContent.has("204")).isTrue();
        assertThat(noContent.path("204").has("content")).isFalse();
    }

    private JsonNode campusOpenApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs/{group}", CAMPUS_GROUP))
            .andExpect(status().isOk())
            .andReturn();
        return JsonAssertions.convertJsonNode(result);
    }

    private JsonNode findResponseSchema(JsonNode response) {
        Iterator<JsonNode> mediaTypes = response.path("content").elements();
        while (mediaTypes.hasNext()) {
            JsonNode schema = mediaTypes.next().path("schema");
            if (!schema.isMissingNode()) {
                return schema;
            }
        }
        throw new AssertionError("Response content does not contain a schema");
    }

}
