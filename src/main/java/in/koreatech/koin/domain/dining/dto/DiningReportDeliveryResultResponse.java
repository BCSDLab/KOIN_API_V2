package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResultRequest.UUID_PATTERN;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dining.model.DiningReportDeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
@Schema(description = "응답 시점의 현재 작업 상태. 제보의 status와 별개입니다.",
    additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
public record DiningReportDeliveryResultResponse(
    @Schema(description = "작업 UUID", format = "uuid", pattern = UUID_PATTERN, requiredMode = REQUIRED)
    UUID deliveryId,
    @Schema(description = "QUEUED는 대기, IN_PROGRESS는 배정, DELIVERED는 삐봇이 성공을 통보한 상태입니다.", requiredMode = REQUIRED)
    DiningReportDeliveryStatus deliveryState
) {
}
