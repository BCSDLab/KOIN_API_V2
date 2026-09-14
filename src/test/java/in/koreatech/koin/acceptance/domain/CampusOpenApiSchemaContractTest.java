package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.support.JsonAssertions;

class CampusOpenApiSchemaContractTest extends AcceptanceTest {

    private static final String CAMPUS_GROUP = "3. Campus API";

    @Test
    void openapi_document_uses_the_declared_3_0_dialect() throws Exception {
        JsonNode openApi = campusOpenApi();

        assertThat(openApi.path("openapi").asText()).isEqualTo("3.0.3");
    }

    @Test
    void every_local_schema_reference_is_resolved() throws Exception {
        JsonNode openApi = campusOpenApi();
        List<String> references = new ArrayList<>();
        collectReferences(openApi, references);

        List<String> unresolved = references.stream()
            .filter(reference -> reference.startsWith("#/components/schemas/"))
            .filter(reference -> openApi.at(reference.substring(1)).isMissingNode())
            .distinct()
            .toList();

        assertThat(unresolved).isEmpty();
    }

    @Test
    void error_response_models_are_registered_as_components() throws Exception {
        JsonNode schemas = campusOpenApi().path("components").path("schemas");

        assertThat(schemas.fieldNames()).toIterable()
            .contains("ErrorResponse", "FieldError");
    }

    private JsonNode campusOpenApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs/{group}", CAMPUS_GROUP))
            .andExpect(status().isOk())
            .andReturn();
        return JsonAssertions.convertJsonNode(result);
    }

    private void collectReferences(JsonNode node, List<String> references) {
        if (node.isObject()) {
            if (node.has("$ref")) {
                references.add(node.path("$ref").asText());
            }
            node.elements().forEachRemaining(child -> collectReferences(child, references));
            return;
        }
        if (node.isArray()) {
            node.elements().forEachRemaining(child -> collectReferences(child, references));
        }
    }
}
