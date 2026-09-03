package in.koreatech.koin.acceptance.domain;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.team.recruitment.dto.ApplicationCreatedResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.ChatMessageResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.ChatRoomResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.TeamRecruitmentChatRoomListItemResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.TeamRecruitmentNotificationResponse;

class TeamRecruitmentOpenApiContractTest extends AcceptanceTest {

    private static final String ISO_LOCAL_DATE_TIME_PATTERN =
        "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?$";
    private static final String SPACE_LOCAL_DATE_TIME_PATTERN =
        "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";

    private static final String APPLY_BLOCK_REASON_PRIORITY = "LOGIN_REQUIRED > OWN_RECRUITMENT > "
        + "ALREADY_APPLIED > RECRUITMENT_CLOSED > DEADLINE_PASSED > ROLE_CLOSED > PROFILE_REQUIRED";

    private static final Set<String> HTTP_METHODS = Set.of("get", "post", "put", "patch", "delete");

    private static final Set<String> TEAM_RECRUITMENT_OPERATIONS = Set.of(
        "GET /team-recruitments",
        "POST /team-recruitments",
        "GET /team-recruitments/{recruitmentId}",
        "PUT /team-recruitments/{recruitmentId}",
        "DELETE /team-recruitments/{recruitmentId}",
        "PUT /team-recruitments/{recruitmentId}/close",
        "GET /team-recruitments/me/created",
        "POST /team-recruitments/{recruitmentId}/applications",
        "GET /team-recruitments/me/applications",
        "GET /team-recruitments/{recruitmentId}/applications",
        "GET /team-recruitments/{recruitmentId}/applications/{applicationId}",
        "PUT /team-recruitments/{recruitmentId}/applications/{applicationId}/status",
        "GET /team-recruitment-profiles/me",
        "PUT /team-recruitment-profiles/me",
        "GET /chatroom/team-recruitment",
        "GET /chatroom/team-recruitment/{recruitmentId}/{chatRoomId}",
        "POST /chatroom/team-recruitment/{recruitmentId}/applications/{applicationId}/direct",
        "GET /chatroom/team-recruitment/{recruitmentId}/{chatRoomId}/messages",
        "POST /chatroom/team-recruitment/{recruitmentId}/{chatRoomId}/messages",
        "GET /team-recruitments/notifications",
        "POST /team-recruitments/notifications/{notificationId}/read",
        "POST /team-recruitments/notifications/mark-all-read",
        "DELETE /team-recruitments/notifications/{notificationId}",
        "DELETE /team-recruitments/notifications"
    );

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void teamRecruitmentContractMatchesRuntime() throws Exception {
        JsonNode openApi = openApi("/v3/api-docs/3.%20Campus%20API");

        assertThat(openApi.path("openapi").asText()).isEqualTo("3.0.3");
        assertThat(teamRecruitmentOperations(openApi)).containsExactlyInAnyOrderElementsOf(
            TEAM_RECRUITMENT_OPERATIONS
        );

        JsonNode chatRoom = schema(openApi, "ChatRoomResponse");
        assertRequired(chatRoom, "chat_room_id", "room_name", "room_type", "status", "member_count",
            "max_member_count", "counterpart");
        assertNullable(chatRoom, "counterpart");
        assertInlineObject(chatRoom, "counterpart", "id", "nickname");

        JsonNode chatRoomListItem = schema(openApi, "TeamRecruitmentChatRoomListItemResponse");
        assertRequired(chatRoomListItem, "recruitment_id", "chat_room_id", "room_name", "room_type", "status",
            "counterpart_id", "counterpart_nickname", "last_message_id", "last_message_content", "last_message_at",
            "last_message_is_image", "unread_message_count");
        assertNullable(chatRoomListItem, "counterpart_id");
        assertNullable(chatRoomListItem, "counterpart_nickname");
        assertNullable(chatRoomListItem, "last_message_id");
        assertNullable(chatRoomListItem, "last_message_content");
        assertNullable(chatRoomListItem, "last_message_at");
        assertNullable(chatRoomListItem, "last_message_is_image");
        assertDateTime(chatRoomListItem, "last_message_at", ISO_LOCAL_DATE_TIME_PATTERN);

        assertRequiredNullable(schema(openApi, "ApplicationCreatedResponse"), "role");
        assertInlineObject(schema(openApi, "ApplicationCreatedResponse"), "role", "id", "name");
        assertRequiredNullable(schema(openApi, "ApplicantDetail"), "role");
        assertRequiredNullable(schema(openApi, "ApplicantSummary"), "role");
        assertRequiredNullable(schema(openApi, "MyApplication"), "role");

        JsonNode directChatRoom = schema(openApi, "DirectChatRoomResponse");
        assertRequired(directChatRoom, "chat_room_id", "room_name", "room_type", "status", "counterpart");
        assertNonNullableObject(openApi, directChatRoom, "counterpart", "id", "nickname");

        JsonNode chatMessage = responseSchema(openApi,
            "/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}/messages", "post", "200");
        assertRequired(chatMessage, "message_id", "user_id", "user_nickname", "content", "timestamp",
            "is_image", "unread_count");
        assertRequired(schema(openApi, "CreateChatMessageRequest"), "content", "is_image");

        JsonNode notification = schema(openApi, "TeamRecruitmentNotificationResponse");
        assertRequired(notification, "id", "type", "target_type", "recruitment_id", "application_id",
            "chat_room_id", "sender_nickname", "message_preview", "is_read", "created_at");
        assertNullable(notification, "application_id");
        assertNullable(notification, "chat_room_id");
        assertNullable(notification, "sender_nickname");
        assertDateTime(notification, "created_at", ISO_LOCAL_DATE_TIME_PATTERN);
        assertRequired(schema(openApi, "TeamRecruitmentNotificationsResponse"), "notifications", "unread_count",
            "total_count", "current_count", "total_page", "current_page");

        assertDateTime(chatMessage, "timestamp", ISO_LOCAL_DATE_TIME_PATTERN);
        assertDateTime(schema(openApi, "ApplicationCreatedResponse"), "created_at",
            SPACE_LOCAL_DATE_TIME_PATTERN);
        assertDateTime(schema(openApi, "RecruitmentDetail"), "created_at",
            SPACE_LOCAL_DATE_TIME_PATTERN);
        assertRequiredNullable(schema(openApi, "RecruitmentCard"), "d_day");
        assertRequiredNullable(schema(openApi, "RecruitmentDetail"), "d_day");
        assertRequiredNullable(schema(openApi, "CreatedRecruitment"), "d_day");
        assertRequiredNullable(schema(openApi, "ApplicantRecruitment"), "d_day");

        assertQueryParameterNames(openApi);

        String blockReasonDescription = schema(openApi, "RecruitmentDetail")
            .path("properties")
            .path("apply_block_reason")
            .path("description")
            .asText();
        assertThat(blockReasonDescription).contains(APPLY_BLOCK_REASON_PRIORITY);
        assertInlineEnum(schema(openApi, "RecruitmentDetail"), "apply_block_reason",
            "RECRUITMENT_DELETED", "LOGIN_REQUIRED", "OWN_RECRUITMENT", "ALREADY_APPLIED",
            "RECRUITMENT_CLOSED", "DEADLINE_PASSED", "ROLE_CLOSED", "PROFILE_REQUIRED");
        assertRequiredNullable(schema(openApi, "RecruitmentDetail"), "application");
        assertInlineObject(schema(openApi, "RecruitmentDetail"), "application", "application_id", "status");
    }

    @Test
    void teamRecruitmentCustomizerIsLimitedToCampusGroup() throws Exception {
        JsonNode defaultOpenApi = openApi("/v3/api-docs");
        JsonNode counterpart = schema(defaultOpenApi, "ChatRoomResponse")
            .path("properties")
            .path("counterpart");

        assertThat(counterpart.path("$ref").asText()).startsWith("#/components/schemas/");
        assertThat(counterpart.has("allOf")).isFalse();
    }

    @Test
    void teamRecruitmentDtosMatchRuntimeJson() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 8, 26, 11, 20, 30, 123_456_000);

        JsonNode application = objectMapper.valueToTree(
            new ApplicationCreatedResponse(51, 17, PENDING, null, timestamp));
        JsonNode chatRoom = objectMapper.valueToTree(
            new ChatRoomResponse(31, "팀 채팅방", "TEAM", "ACTIVE", 1, 2, null));
        JsonNode chatRoomListItem = objectMapper.valueToTree(new TeamRecruitmentChatRoomListItemResponse(
            17, 31, "팀 채팅방", "TEAM", "ACTIVE", null, null,
            null, null, null, null, 0));
        JsonNode notification = objectMapper.valueToTree(new TeamRecruitmentNotificationResponse(
            101, "NEW_APPLICATION", "APPLICANT_MANAGEMENT", 17, null, null, null,
            "새로운 지원자가 있어요.", false, timestamp));
        JsonNode message = objectMapper.valueToTree(
            new ChatMessageResponse(901, 22, "김철수", "안녕하세요!", timestamp, false, 0));

        assertThat(application.path("role").isNull()).isTrue();
        assertThat(application.path("created_at").asText()).isEqualTo("2026-08-26 11:20:30");
        assertThat(chatRoom.path("counterpart").isNull()).isTrue();
        assertThat(chatRoomListItem.path("counterpart_id").isNull()).isTrue();
        assertThat(chatRoomListItem.path("counterpart_nickname").isNull()).isTrue();
        assertThat(chatRoomListItem.path("last_message_id").isNull()).isTrue();
        assertThat(chatRoomListItem.path("last_message_content").isNull()).isTrue();
        assertThat(chatRoomListItem.path("last_message_at").isNull()).isTrue();
        assertThat(chatRoomListItem.path("last_message_is_image").isNull()).isTrue();
        assertThat(notification.path("application_id").isNull()).isTrue();
        assertThat(notification.path("chat_room_id").isNull()).isTrue();
        assertThat(notification.path("sender_nickname").isNull()).isTrue();
        assertThat(notification.path("created_at").asText()).isEqualTo("2026-08-26T11:20:30.123456");
        assertThat(message.path("timestamp").asText()).isEqualTo("2026-08-26T11:20:30.123456");
    }

    private JsonNode openApi(String path) throws Exception {
        String document = mockMvc.perform(get(URI.create(path)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(document);
    }

    private Set<String> teamRecruitmentOperations(JsonNode openApi) {
        Set<String> operations = new HashSet<>();
        Iterator<Map.Entry<String, JsonNode>> paths = openApi.path("paths").fields();
        while (paths.hasNext()) {
            Map.Entry<String, JsonNode> path = paths.next();
            if (!isTeamRecruitmentPath(path.getKey())) {
                continue;
            }
            path.getValue().fieldNames().forEachRemaining(method -> {
                if (HTTP_METHODS.contains(method)) {
                    operations.add(method.toUpperCase() + " " + path.getKey());
                }
            });
        }
        return operations;
    }

    private boolean isTeamRecruitmentPath(String path) {
        return path.startsWith("/team-recruitments")
            || path.startsWith("/team-recruitment-profiles")
            || path.startsWith("/chatroom/team-recruitment");
    }

    private JsonNode schema(JsonNode openApi, String name) {
        JsonNode schema = openApi.path("components").path("schemas").path(name);
        assertThat(schema.isMissingNode())
            .as("OpenAPI component schema %s", name)
            .isFalse();
        return schema;
    }

    private JsonNode responseSchema(JsonNode openApi, String path, String method, String status) {
        JsonNode content = openApi.path("paths")
            .path(path)
            .path(method)
            .path("responses")
            .path(status)
            .path("content");
        JsonNode mediaType = content.path("application/json");
        if (mediaType.isMissingNode()) {
            mediaType = content.elements().next();
        }
        JsonNode responseSchema = mediaType.path("schema");
        String reference = responseSchema.path("$ref").asText();
        assertThat(reference)
            .as("response schema: %s", responseSchema)
            .startsWith("#/components/schemas/");
        return schema(openApi, reference.substring(reference.lastIndexOf('/') + 1));
    }

    private void assertRequiredNullable(JsonNode schema, String property) {
        assertRequired(schema, property);
        assertNullable(schema, property);
    }

    private void assertRequired(JsonNode schema, String... properties) {
        assertThat(schema.path("required"))
            .extracting(JsonNode::asText)
            .contains(properties);
    }

    private void assertNullable(JsonNode schema, String property) {
        JsonNode propertySchema = schema.path("properties").path(property);
        assertThat(propertySchema.path("nullable").asBoolean())
            .as("%s must be nullable: %s", property, propertySchema)
            .isTrue();
        assertThat(propertySchema.path("type").asText())
            .as("%s must declare its type next to nullable: %s", property, propertySchema)
            .isNotBlank();
        assertThat(propertySchema.has("$ref")).isFalse();
        assertThat(propertySchema.has("allOf")).isFalse();
    }

    private void assertInlineObject(JsonNode schema, String property, String... requiredProperties) {
        JsonNode propertySchema = schema.path("properties").path(property);
        assertThat(propertySchema.path("type").asText()).isEqualTo("object");
        assertThat(propertySchema.path("properties").fieldNames())
            .toIterable()
            .contains(requiredProperties);
        assertRequired(propertySchema, requiredProperties);
    }

    private void assertNonNullableObject(
        JsonNode openApi,
        JsonNode schema,
        String property,
        String... requiredProperties
    ) {
        JsonNode propertySchema = schema.path("properties").path(property);
        String reference = propertySchema.path("$ref").asText();
        JsonNode effectiveSchema = reference.isBlank()
            ? propertySchema
            : openApi.at(reference.substring(1));

        assertThat(propertySchema.path("nullable").asBoolean()).isFalse();
        assertThat(effectiveSchema.path("nullable").asBoolean()).isFalse();
        assertThat(effectiveSchema.path("type").asText()).isEqualTo("object");
        assertThat(effectiveSchema.path("properties").fieldNames())
            .toIterable()
            .contains(requiredProperties);
        assertRequired(effectiveSchema, requiredProperties);
    }

    private void assertInlineEnum(JsonNode schema, String property, String... values) {
        JsonNode propertySchema = schema.path("properties").path(property);
        assertThat(propertySchema.path("type").asText()).isEqualTo("string");
        assertThat(propertySchema.path("enum"))
            .extracting(JsonNode::asText)
            .containsExactly(values);
    }

    private void assertQueryParameterNames(JsonNode openApi) {
        JsonNode parameters = openApi.path("paths")
            .path("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}/messages")
            .path("get")
            .path("parameters");
        Set<String> queryNames = new HashSet<>();
        parameters.elements().forEachRemaining(parameter -> {
            if ("query".equals(parameter.path("in").asText())) {
                queryNames.add(parameter.path("name").asText());
            }
        });
        assertThat(queryNames).containsExactlyInAnyOrder("afterMessageId", "beforeMessageId", "limit");
    }

    private void assertDateTime(JsonNode schema, String property, String pattern) {
        JsonNode dateTime = schema.path("properties").path(property);
        assertThat(dateTime.path("type").asText()).as("date-time schema: %s", dateTime).isEqualTo("string");
        assertThat(dateTime.path("format").isMissingNode()).as("date-time schema: %s", dateTime).isTrue();
        assertThat(dateTime.path("pattern").asText()).as("date-time schema: %s", dateTime).isEqualTo(pattern);
    }
}
