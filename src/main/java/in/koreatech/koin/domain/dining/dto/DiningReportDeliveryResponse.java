package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResultRequest.UUID_PATTERN;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "배정된 작업 한 건. report는 변경 시점의 내용으로 고정됩니다.",
    additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
public record DiningReportDeliveryResponse(
    @Schema(description = "재배정되어도 유지되는 작업 UUID", format = "uuid", pattern = UUID_PATTERN, requiredMode = REQUIRED)
    UUID deliveryId,
    @Schema(description = "이번 시도의 비공개 토큰. 결과 통보에만 사용합니다.", format = "uuid", pattern = UUID_PATTERN, requiredMode = REQUIRED)
    UUID attemptToken,
    @Schema(description = "배정 후 60초가 되는 한국시간. 이 시각부터 작업을 다시 배정할 수 있습니다.",
        type = "string", format = "date-time", example = "2026-10-06T12:42:00+09:00", requiredMode = REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    OffsetDateTime expiresAt,
    @Schema(description = "해당 변경의 제보 내용. 상세 조회 결과로 대체하지 않습니다.", requiredMode = REQUIRED)
    DiningReportResponse report
) {
}
