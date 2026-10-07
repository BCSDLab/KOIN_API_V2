package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResultRequest.UUID_PATTERN;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dining.model.DiningReportDeliveryMode;
import in.koreatech.koin.domain.dining.model.DiningReportDeliveryOperation;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "배정된 전송 작업 한 건. report, operation, target은 배정 시점의 값으로 고정됩니다.",
    additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
public record DiningReportDeliveryResponse(
    @Schema(description = "전송 작업과 메시지 내용을 구분하는 UUID", format = "uuid", pattern = UUID_PATTERN, requiredMode = REQUIRED)
    UUID deliveryId,
    @Schema(description = "이번 시도의 비공개 토큰. 결과 통보에만 사용합니다.", format = "uuid", pattern = UUID_PATTERN, requiredMode = REQUIRED)
    UUID attemptToken,
    @Schema(description = "SEND는 생성이나 수정, VERIFY는 기존 메시지 확인만 수행합니다.", requiredMode = REQUIRED)
    DiningReportDeliveryMode mode,
    @Schema(description = "CREATE는 새 메시지 생성, UPDATE는 연결된 메시지 수정입니다.", requiredMode = REQUIRED)
    DiningReportDeliveryOperation operation,
    @Schema(description = "배정 후 60초가 되는 한국시간. 이후 생성이나 수정 요청을 새로 시작하지 않습니다.",
        type = "string", format = "date-time", example = "2026-10-06T12:42:00+09:00", requiredMode = REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    OffsetDateTime expiresAt,
    @Schema(description = "백엔드에 저장된 메시지 대상", requiredMode = REQUIRED)
    Target target,
    @Schema(description = "배정 시점의 제보 내용. 상세 조회 결과로 대체하지 않습니다.", requiredMode = REQUIRED)
    DiningReportResponse report
) {
    @JsonNaming(SnakeCaseStrategy.class)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @Schema(name = "DiningReportDeliveryMessageTarget", additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
    public record Target(
        @Schema(description = "전송 대상 워크스페이스", minLength = 1, requiredMode = REQUIRED)
        String workspaceId,
        @Schema(description = "전송 대상 채널. 스레드로 보내지 않습니다.", minLength = 1, requiredMode = REQUIRED)
        String channelId,
        @Schema(description = "CREATE이면 null, UPDATE이면 저장된 슬랙 ts 문자열입니다. 숫자로 변환하지 않습니다.",
            nullable = true, requiredMode = REQUIRED)
        String messageTs
    ) {
    }
}
