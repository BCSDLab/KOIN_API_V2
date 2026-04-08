package in.koreatech.koin.domain.callvan.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.callvan.model.CallvanReportProcess;
import in.koreatech.koin.domain.callvan.model.enums.CallvanReportProcessType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record CallvanRestrictionResponse(
    @Schema(description = "콜벤 기능 이용 제한 여부", example = "true", requiredMode = REQUIRED)
    boolean isRestricted,

    @Schema(
        description = "현재 활성화된 이용 제한 유형. 제한이 없으면 null이며 WARNING(1차 경고)는 반환되지 않습니다.",
        example = "TEMPORARY_RESTRICTION_14_DAYS"
    )
    CallvanReportProcessType restrictionType,

    @Schema(
        description = "임시 이용 제한 종료 시각. 영구 제한 또는 미제한이면 null입니다.",
        example = "2026-04-21T12:00:00"
    )
    LocalDateTime restrictedUntil
) {

    public static CallvanRestrictionResponse from(CallvanReportProcess process) {
        return new CallvanRestrictionResponse(
            true,
            process.getProcessType(),
            process.getRestrictedUntil()
        );
    }

    public static CallvanRestrictionResponse unrestricted() {
        return new CallvanRestrictionResponse(false, null, null);
    }
}
