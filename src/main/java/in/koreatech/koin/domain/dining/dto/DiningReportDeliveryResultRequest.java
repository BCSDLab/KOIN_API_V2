package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_READABLE_HTTP_MESSAGE;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dining.model.DiningReportDeliveryOutcome;
import in.koreatech.koin.global.exception.CustomException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
@Schema(description = "배정받은 시도의 토큰과 결과만 전달합니다. 다른 필드는 null이어도 허용하지 않습니다.",
    additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
public record DiningReportDeliveryResultRequest(
    @Schema(description = "작업 배정에서 받은 이번 시도의 비공개 토큰", format = "uuid",
        pattern = DiningReportDeliveryResultRequest.UUID_PATTERN,
        requiredMode = REQUIRED)
    @NotNull UUID attemptToken,
    @Schema(description = "삐봇이 통보하는 작업 결과", requiredMode = REQUIRED)
    @NotNull DiningReportDeliveryOutcome outcome
) {
    public static final String UUID_PATTERN =
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    private static final Set<String> FIELDS = Set.of("attempt_token", "outcome");

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DiningReportDeliveryResultRequest fromJson(JsonNode body) {
        if (body == null || body.isNull()) {
            throw CustomException.of(INVALID_REQUEST_BODY);
        }
        if (!body.isObject()) {
            throw CustomException.of(NOT_READABLE_HTTP_MESSAGE);
        }
        body.fieldNames().forEachRemaining(field -> {
            if (!FIELDS.contains(field)) {
                throw CustomException.of(INVALID_REQUEST_BODY);
            }
        });
        String token = requiredText(body, "attempt_token");
        if (!token.matches(UUID_PATTERN)) {
            throw CustomException.of(NOT_READABLE_HTTP_MESSAGE);
        }
        String outcome = requiredText(body, "outcome");
        try {
            return new DiningReportDeliveryResultRequest(UUID.fromString(token),
                DiningReportDeliveryOutcome.valueOf(outcome));
        } catch (IllegalArgumentException e) {
            throw CustomException.of(NOT_READABLE_HTTP_MESSAGE);
        }
    }

    private static String requiredText(JsonNode body, String field) {
        JsonNode value = body.get(field);
        if (value == null || value.isNull()) {
            throw CustomException.of(INVALID_REQUEST_BODY);
        }
        if (!value.isTextual()) {
            throw CustomException.of(NOT_READABLE_HTTP_MESSAGE);
        }
        if (value.textValue().isEmpty()) {
            throw CustomException.of(INVALID_REQUEST_BODY);
        }
        return value.textValue();
    }
}
