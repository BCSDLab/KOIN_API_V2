package in.koreatech.koin.global.config;

import java.util.List;
import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class TeamRecruitmentOpenApiCustomizer implements OpenApiCustomizer {

    private static final String ISO_LOCAL_DATE_TIME_PATTERN =
        "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?$";
    private static final String SPACE_LOCAL_DATE_TIME_PATTERN =
        "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";

    private static final List<NullableReference> NULLABLE_REFERENCES = List.of(
        new NullableReference("ChatRoomResponse", "counterpart"),
        new NullableReference("ApplicationCreatedResponse", "role"),
        new NullableReference("ApplicantDetail", "role"),
        new NullableReference("ApplicantSummary", "role"),
        new NullableReference("MyApplication", "role"),
        new NullableReference("RecruitmentDetail", "apply_block_reason"),
        new NullableReference("RecruitmentDetail", "application")
    );

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }
        Map<String, Schema> schemas = openApi.getComponents().getSchemas();
        NULLABLE_REFERENCES.forEach(reference -> makeNullable(schemas, reference));
        configureDateTime(
            schemas,
            "TeamRecruitmentNotificationResponse",
            "created_at",
            ISO_LOCAL_DATE_TIME_PATTERN
        );
        configureDateTime(schemas, "ApplicationCreatedResponse", "created_at", SPACE_LOCAL_DATE_TIME_PATTERN);
        configureDateTime(schemas, "RecruitmentDetail", "created_at", SPACE_LOCAL_DATE_TIME_PATTERN);
        configureDateTime(
            schemas,
            "TeamRecruitmentChatRoomListItemResponse",
            "last_message_at",
            ISO_LOCAL_DATE_TIME_PATTERN
        );
        configureChatMessageTimestamp(openApi, schemas);
    }

    private void makeNullable(Map<String, Schema> schemas, NullableReference reference) {
        Schema<?> property = propertyOf(schemas, reference.schemaName(), reference.propertyName());
        if (property == null) {
            return;
        }
        Schema<?> source = property;
        if (property.get$ref() != null && !property.get$ref().isBlank()) {
            String schemaName = property.get$ref().substring(property.get$ref().lastIndexOf('/') + 1);
            source = schemas.get(schemaName);
        }
        if (source == null) {
            return;
        }
        Schema<?> nullableSchema = Json.mapper().convertValue(source, Schema.class);
        nullableSchema.setNullable(true);
        if (property.getDescription() != null) {
            nullableSchema.setDescription(property.getDescription());
        }
        Schema<?> owner = schemas.get(reference.schemaName());
        owner.addProperty(reference.propertyName(), nullableSchema);
    }

    private void configureChatMessageTimestamp(OpenAPI openApi, Map<String, Schema> schemas) {
        if (openApi.getPaths() == null) {
            return;
        }
        PathItem path = openApi.getPaths()
            .get("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}/messages");
        if (path == null || path.getPost() == null) {
            return;
        }
        ApiResponse ok = path.getPost().getResponses().get("200");
        if (ok == null || ok.getContent() == null || ok.getContent().isEmpty()) {
            return;
        }
        MediaType mediaType = ok.getContent().get("application/json");
        if (mediaType == null) {
            mediaType = ok.getContent().values().iterator().next();
        }
        Schema<?> response = mediaType.getSchema();
        if (response == null || response.get$ref() == null) {
            return;
        }
        String schemaName = response.get$ref().substring(response.get$ref().lastIndexOf('/') + 1);
        configureDateTime(schemas, schemaName, "timestamp", ISO_LOCAL_DATE_TIME_PATTERN);
    }

    private void configureDateTime(
        Map<String, Schema> schemas,
        String schemaName,
        String propertyName,
        String pattern
    ) {
        Schema<?> dateTime = propertyOf(schemas, schemaName, propertyName);
        if (dateTime == null) {
            return;
        }
        dateTime.setFormat(null);
        dateTime.setPattern(pattern);
    }

    private Schema<?> propertyOf(Map<String, Schema> schemas, String schemaName, String propertyName) {
        Schema<?> schema = schemas.get(schemaName);
        return schema == null || schema.getProperties() == null
            ? null
            : (Schema<?>)schema.getProperties().get(propertyName);
    }

    private record NullableReference(String schemaName, String propertyName) {
    }
}
