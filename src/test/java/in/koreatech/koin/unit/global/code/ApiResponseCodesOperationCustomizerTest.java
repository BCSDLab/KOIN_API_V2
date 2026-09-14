package in.koreatech.koin.unit.global.code;

import static in.koreatech.koin.global.code.ApiResponseCode.CREATED;
import static in.koreatech.koin.global.code.ApiResponseCode.ILLEGAL_ARGUMENT;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.NO_CONTENT;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.HandlerMethod;

import in.koreatech.koin.domain.teamrecruitment.controller.TeamRecruitmentChatController;
import in.koreatech.koin.global.code.ApiResponseCodes;
import in.koreatech.koin.global.code.ApiResponseCodesOperationCustomizer;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

class ApiResponseCodesOperationCustomizerTest {

    private final ApiResponseCodesOperationCustomizer customizer = new ApiResponseCodesOperationCustomizer();

    @Test
    void same_status_codes_are_grouped_and_no_content_has_no_media_type() throws Exception {
        Operation operation = new Operation().responses(new ApiResponses());
        Method method = SampleController.class.getDeclaredMethod("update");

        customizer.customize(operation, new HandlerMethod(new SampleController(), method));

        assertThat(operation.getResponses()).containsOnlyKeys("204", "400");

        ApiResponse noContent = operation.getResponses().get("204");
        assertThat(noContent.getContent()).isNull();

        ApiResponse badRequest = operation.getResponses().get("400");
        MediaType mediaType = badRequest.getContent().get("application/json");
        assertThat(mediaType.getExamples()).containsOnlyKeys("INVALID_REQUEST_BODY", "ILLEGAL_ARGUMENT");
        assertThat(((Map<?, ?>) mediaType.getExamples().get("INVALID_REQUEST_BODY").getValue()).get("code"))
            .isEqualTo("INVALID_REQUEST_BODY");
        assertThat(((Map<?, ?>) mediaType.getExamples().get("ILLEGAL_ARGUMENT").getValue()).get("code"))
            .isEqualTo("ILLEGAL_ARGUMENT");
    }

    @Test
    void void_success_response_has_no_media_type_even_when_status_is_not_204() throws Exception {
        Operation operation = new Operation().responses(new ApiResponses());
        Method method = SampleController.class.getDeclaredMethod("acknowledge");

        customizer.customize(operation, new HandlerMethod(new SampleController(), method));

        assertThat(operation.getResponses().get("200").getContent()).isNull();
    }

    @Test
    void team_recruitment_chat_annotation_uses_standard_status_keys() throws Exception {
        Operation operation = new Operation().responses(new ApiResponses());
        Method method = TeamRecruitmentChatController.class.getMethod(
            "getOrCreateDirectChatRoom", Integer.class, Integer.class, Integer.class);

        customizer.customize(
            operation,
            new HandlerMethod(new TeamRecruitmentChatController(null), method)
        );

        assertThat(operation.getResponses()).containsOnlyKeys("200", "201", "401", "403", "404", "409");
    }

    @Test
    void existing_root_list_success_schema_and_description_are_preserved() throws Exception {
        Content springdocContent = new Content().addMediaType(
            "*/*",
            new MediaType().schema(new ArraySchema().items(
                new Schema<>().$ref("#/components/schemas/SampleResponse")))
        );
        ApiResponse springdocResponse = new ApiResponse()
            .description("Springdoc success")
            .content(springdocContent);
        Operation operation = new Operation().responses(
            new ApiResponses().addApiResponse("200", springdocResponse));
        Method method = SampleController.class.getDeclaredMethod("list");

        customizer.customize(operation, new HandlerMethod(new SampleController(), method));

        ApiResponse response = operation.getResponses().get("200");
        assertThat(response.getContent()).isSameAs(springdocContent);
        assertThat(response.getDescription()).contains("Springdoc success", "OK:");
        Schema<?> schema = response.getContent().get("*/*").getSchema();
        assertThat(schema.getType()).isEqualTo("array");
        assertThat(schema.getItems().get$ref()).isEqualTo("#/components/schemas/SampleResponse");
    }

    @Test
    void phantom_200_is_removed_and_generated_schema_is_moved_to_created_response() throws Exception {
        Content springdocContent = new Content().addMediaType(
            "*/*",
            new MediaType().schema(new Schema<>().$ref("#/components/schemas/SampleResponse"))
        );
        Operation operation = new Operation().responses(new ApiResponses().addApiResponse(
            "200",
            new ApiResponse().description("Springdoc success").content(springdocContent)
        ));
        Method method = SampleController.class.getDeclaredMethod("create");

        customizer.customize(operation, new HandlerMethod(new SampleController(), method));

        assertThat(operation.getResponses()).containsOnlyKeys("201");
        assertThat(operation.getResponses().get("201").getContent()).isSameAs(springdocContent);
    }

    @Test
    void existing_created_schema_and_description_take_precedence_over_phantom_200() throws Exception {
        Content springdocContent = new Content().addMediaType(
            "*/*",
            new MediaType().schema(new Schema<>().$ref("#/components/schemas/SampleResponse"))
        );
        Content documentedCreatedContent = new Content().addMediaType(
            "application/json",
            new MediaType().schema(new Schema<>().$ref("#/components/schemas/DocumentedCreatedResponse"))
        );
        Operation operation = new Operation().responses(new ApiResponses()
            .addApiResponse("200", new ApiResponse().description("Springdoc success").content(springdocContent))
            .addApiResponse("201", new ApiResponse()
                .description("Manually documented creation")
                .content(documentedCreatedContent)));
        Method method = SampleController.class.getDeclaredMethod("create");

        customizer.customize(operation, new HandlerMethod(new SampleController(), method));

        assertThat(operation.getResponses()).containsOnlyKeys("201");
        ApiResponse created = operation.getResponses().get("201");
        assertThat(created.getContent()).isSameAs(documentedCreatedContent);
        assertThat(created.getDescription()).contains("Manually documented creation", "CREATED:");
    }

    @Test
    void phantom_200_is_removed_from_no_content_response() throws Exception {
        Operation operation = new Operation().responses(new ApiResponses().addApiResponse(
            "200",
            new ApiResponse().description("Springdoc success")
        ));
        Method method = SampleController.class.getDeclaredMethod("remove");

        customizer.customize(operation, new HandlerMethod(new SampleController(), method));

        assertThat(operation.getResponses()).containsOnlyKeys("204");
        assertThat(operation.getResponses().get("204").getContent()).isNull();
    }

    @Test
    void existing_error_examples_are_preserved_when_domain_examples_are_merged() throws Exception {
        Example existing = new Example().summary("manually documented example");
        Map<String, String> singleExample = Map.of("source", "manual");
        Schema<?> existingSchema = new Schema<>().type("object");
        MediaType existingMediaType = new MediaType()
            .schema(existingSchema)
            .example(singleExample)
            .addExamples("ILLEGAL_ARGUMENT", existing);
        Operation operation = new Operation().responses(new ApiResponses().addApiResponse(
            "400",
            new ApiResponse()
                .description("Manually documented bad request")
                .content(new Content().addMediaType("application/json", existingMediaType))
        ));
        Method method = SampleController.class.getDeclaredMethod("update");

        customizer.customize(operation, new HandlerMethod(new SampleController(), method));

        ApiResponse response = operation.getResponses().get("400");
        assertThat(response.getDescription()).contains("Manually documented bad request", "INVALID_REQUEST_BODY:");
        assertThat(response.getContent().get("application/json").getSchema()).isSameAs(existingSchema);
        assertThat(response.getContent().get("application/json").getExamples())
            .containsEntry("ILLEGAL_ARGUMENT", existing)
            .containsKey("INVALID_REQUEST_BODY");
        assertThat(response.getContent().get("application/json").getExample()).isNull();
        assertThat(response.getContent().get("application/json").getExamples().values())
            .extracting(Example::getValue)
            .contains(singleExample);
    }

    private static class SampleController {

        @ApiResponseCodes({NO_CONTENT, INVALID_REQUEST_BODY, ILLEGAL_ARGUMENT})
        ResponseEntity<Void> update() {
            return ResponseEntity.noContent().build();
        }

        @ApiResponseCodes(OK)
        ResponseEntity<Void> acknowledge() {
            return ResponseEntity.ok().build();
        }

        @ApiResponseCodes(OK)
        ResponseEntity<List<SampleResponse>> list() {
            return ResponseEntity.ok(List.of());
        }

        @ApiResponseCodes(CREATED)
        ResponseEntity<SampleResponse> create() {
            return ResponseEntity.status(201).body(new SampleResponse("created"));
        }

        @ApiResponseCodes(NO_CONTENT)
        ResponseEntity<Void> remove() {
            return ResponseEntity.noContent().build();
        }
    }

    private record SampleResponse(String value) {}
}
