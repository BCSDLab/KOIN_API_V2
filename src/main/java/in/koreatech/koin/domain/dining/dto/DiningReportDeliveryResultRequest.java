package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_READABLE_HTTP_MESSAGE;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dining.model.DiningReportDeliveryFailureReason;
import in.koreatech.koin.domain.dining.model.DiningReportDeliveryOutcome;
import in.koreatech.koin.global.exception.CustomException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "결과에 맞는 본문 하나만 전달합니다. NOT_APPLIED는 SEND에서만 허용됩니다. "
    + "각 형식에 없는 필드는 null이어도 전달할 수 없습니다.",
    oneOf = {
        DiningReportDeliveryResultRequest.Succeeded.class,
        DiningReportDeliveryResultRequest.NotSent.class,
        DiningReportDeliveryResultRequest.RateLimited.class,
        DiningReportDeliveryResultRequest.Rejected.class,
        DiningReportDeliveryResultRequest.Uncertain.class
    }, additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
public record DiningReportDeliveryResultRequest(
    @Schema(description = "작업 배정에서 받은 이번 시도의 비공개 토큰", format = "uuid", requiredMode = REQUIRED)
    @NotNull UUID attemptToken,
    @Schema(description = "삐봇이 확인한 전송 결과", requiredMode = REQUIRED)
    @NotNull DiningReportDeliveryOutcome outcome,
    @Schema(description = "SUCCEEDED일 때만 필수이며, 그 외에는 전달하지 않습니다.")
    @Valid MessageReference messageRef,
    @Schema(description = "NOT_APPLIED일 때만 필수이며, 그 외에는 전달하지 않습니다.")
    DiningReportDeliveryFailureReason reason,
    @Schema(description = "RATE_LIMITED일 때만 필수인 슬랙 Retry-After 초", minimum = "1")
    @Min(1) Long retryAfterSeconds,
    @Schema(description = "REJECTED일 때만 필수인 슬랙 오류 코드", minLength = 1, maxLength = 128)
    String errorCode
) {
    public static final String UUID_PATTERN =
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DiningReportDeliveryResultRequest fromJson(JsonNode body) {
        requireObject(body);
        String token = requiredText(body, "attempt_token");
        if (!token.matches(UUID_PATTERN)) {
            throw CustomException.of(NOT_READABLE_HTTP_MESSAGE);
        }
        UUID attemptToken = UUID.fromString(token);
        DiningReportDeliveryOutcome outcome = requiredEnum(body, "outcome", DiningReportDeliveryOutcome.class);
        return switch (outcome) {
            case SUCCEEDED -> {
                requireFields(body, "attempt_token", "outcome", "message_ref");
                JsonNode message = body.get("message_ref");
                requireFields(message, "channel_id", "message_ts");
                yield new DiningReportDeliveryResultRequest(attemptToken, outcome,
                    new MessageReference(requiredText(message, "channel_id"), requiredText(message, "message_ts")),
                    null, null, null);
            }
            case NOT_APPLIED -> {
                DiningReportDeliveryFailureReason reason = requiredEnum(body, "reason",
                    DiningReportDeliveryFailureReason.class);
                Long retryAfterSeconds = null;
                String errorCode = null;
                switch (reason) {
                    case NOT_SENT -> requireFields(body, "attempt_token", "outcome", "reason");
                    case RATE_LIMITED -> {
                        requireFields(body, "attempt_token", "outcome", "reason", "retry_after_seconds");
                        JsonNode retry = body.get("retry_after_seconds");
                        if (!retry.isIntegralNumber() || !retry.canConvertToLong()) {
                            throw CustomException.of(NOT_READABLE_HTTP_MESSAGE);
                        }
                        retryAfterSeconds = retry.longValue();
                        if (retryAfterSeconds < 1) {
                            throw CustomException.of(INVALID_REQUEST_BODY);
                        }
                    }
                    case REJECTED -> {
                        requireFields(body, "attempt_token", "outcome", "reason", "error_code");
                        errorCode = requiredText(body, "error_code");
                        if (errorCode.codePointCount(0, errorCode.length()) > 128) {
                            throw CustomException.of(INVALID_REQUEST_BODY);
                        }
                    }
                }
                yield new DiningReportDeliveryResultRequest(attemptToken, outcome, null, reason,
                    retryAfterSeconds, errorCode);
            }
            case UNCERTAIN -> {
                requireFields(body, "attempt_token", "outcome");
                yield new DiningReportDeliveryResultRequest(attemptToken, outcome, null, null, null, null);
            }
        };
    }

    private static void requireObject(JsonNode value) {
        if (value == null || value.isNull()) {
            throw CustomException.of(INVALID_REQUEST_BODY);
        }
        if (!value.isObject()) {
            throw CustomException.of(NOT_READABLE_HTTP_MESSAGE);
        }
    }

    private static void requireFields(JsonNode value, String... fieldNames) {
        requireObject(value);
        Set<String> required = Set.of(fieldNames);
        for (Iterator<String> fields = value.fieldNames(); fields.hasNext();) {
            if (!required.contains(fields.next())) {
                throw CustomException.of(INVALID_REQUEST_BODY);
            }
        }
        for (String field : required) {
            if (!value.hasNonNull(field)) {
                throw CustomException.of(INVALID_REQUEST_BODY);
            }
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

    private static <E extends Enum<E>> E requiredEnum(JsonNode body, String field, Class<E> enumType) {
        String value = requiredText(body, field);
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException e) {
            throw CustomException.of(NOT_READABLE_HTTP_MESSAGE);
        }
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(name = "DiningReportDeliveryMessageReference", additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record MessageReference(
        @Schema(description = "확인한 메시지의 실제 채널", minLength = 1, requiredMode = REQUIRED)
        @NotNull @Size(min = 1) String channelId,
        @Schema(description = "슬랙 ts 원문 문자열. 숫자로 변환하지 않습니다.", minLength = 1, requiredMode = REQUIRED)
        @NotNull @Size(min = 1) String messageTs
    ) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(name = "DiningReportDeliverySucceeded", additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        requiredProperties = {"attempt_token", "outcome", "message_ref"})
    public record Succeeded(
        @Schema(format = "uuid", pattern = UUID_PATTERN) UUID attemptToken,
        @Schema(allowableValues = "SUCCEEDED") String outcome,
        MessageReference messageRef
    ) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(name = "DiningReportDeliveryNotSent", additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        requiredProperties = {"attempt_token", "outcome", "reason"})
    public record NotSent(
        @Schema(format = "uuid", pattern = UUID_PATTERN) UUID attemptToken,
        @Schema(allowableValues = "NOT_APPLIED") String outcome,
        @Schema(allowableValues = "NOT_SENT") String reason
    ) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(name = "DiningReportDeliveryRateLimited", additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        requiredProperties = {"attempt_token", "outcome", "reason", "retry_after_seconds"})
    public record RateLimited(
        @Schema(format = "uuid", pattern = UUID_PATTERN) UUID attemptToken,
        @Schema(allowableValues = "NOT_APPLIED") String outcome,
        @Schema(allowableValues = "RATE_LIMITED") String reason,
        @Schema(minimum = "1") Long retryAfterSeconds
    ) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(name = "DiningReportDeliveryRejected", additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        requiredProperties = {"attempt_token", "outcome", "reason", "error_code"})
    public record Rejected(
        @Schema(format = "uuid", pattern = UUID_PATTERN) UUID attemptToken,
        @Schema(allowableValues = "NOT_APPLIED") String outcome,
        @Schema(allowableValues = "REJECTED") String reason,
        @Schema(minLength = 1, maxLength = 128) String errorCode
    ) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(name = "DiningReportDeliveryUncertain", additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        requiredProperties = {"attempt_token", "outcome"})
    public record Uncertain(
        @Schema(format = "uuid", pattern = UUID_PATTERN) UUID attemptToken,
        @Schema(allowableValues = "UNCERTAIN") String outcome
    ) {
    }
}
