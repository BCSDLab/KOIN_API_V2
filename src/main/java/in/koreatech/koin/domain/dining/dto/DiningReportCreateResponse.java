package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dining.model.DiningReport;
import in.koreatech.koin.domain.dining.model.DiningReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record DiningReportCreateResponse(
    @Schema(description = "제보 ID. 동일 요청 키의 재시도에서도 최초 ID를 유지합니다.", example = "1", requiredMode = REQUIRED)
    Integer reportId,
    @Schema(description = "최초 접수 상태. 재시도에서도 최초 접수 결과를 반환합니다.", example = "PENDING",
        allowableValues = {"PENDING"}, requiredMode = REQUIRED)
    DiningReportStatus status,
    @Schema(description = "최초 접수 시각 (한국시간). 재시도에서도 유지합니다.", example = "2026-10-02 12:00:00",
        implementation = String.class, type = "string", pattern = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$",
        requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt
) {
    public static DiningReportCreateResponse from(DiningReport report) {
        return new DiningReportCreateResponse(report.getId(), DiningReportStatus.PENDING, report.getCreatedAt());
    }
}
