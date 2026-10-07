package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dining.model.DiningReport;
import in.koreatech.koin.domain.dining.model.DiningReportProcessingType;
import in.koreatech.koin.domain.dining.model.DiningReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.ALWAYS)
public record DiningReportSummaryResponse(
    @Schema(description = "제보 ID", example = "1", requiredMode = REQUIRED)
    Integer reportId,
    @Schema(description = "제보 대상 식단", requiredMode = REQUIRED)
    DiningReportResponse.DiningReference dining,
    @Schema(description = "제보 상태. PENDING은 대기, APPROVED는 승인, REJECTED는 반려입니다.",
        example = "APPROVED", requiredMode = REQUIRED)
    DiningReportStatus status,
    @Schema(description = "MANUAL은 담당자 처리, SAME_DINING_APPROVED는 같은 식단 승인에 따른 자동 승인, "
        + "COOP_PREPROCESSED는 영양사 품절에 따른 자동 반려입니다. 대기 중이면 null입니다.",
        example = "MANUAL", nullable = true, requiredMode = REQUIRED)
    DiningReportProcessingType processingType,
    @Schema(description = "처리 사유. 대기 중이면 null입니다.", example = "담당자 승인", nullable = true, requiredMode = REQUIRED)
    String reason,
    @Schema(description = "접수 시각 (한국시간)", example = "2026-10-02 12:00:00",
        implementation = String.class, type = "string", pattern = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$",
        requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
    @Schema(description = "담당자 수동 처리 시각 (한국시간). 대기·자동 처리이면 null입니다.", example = "2026-10-02 12:01:00",
        implementation = String.class, type = "string", pattern = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$",
        nullable = true, requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime processedAt,
    @Schema(description = "담당자 수동 처리의 감사 정보. 대기·자동 처리이면 null입니다.", nullable = true, requiredMode = REQUIRED)
    DiningReportActor processor
) {
    public static DiningReportSummaryResponse from(DiningReport report) {
        DiningReportResponse detail = DiningReportResponse.from(report);
        return new DiningReportSummaryResponse(detail.reportId(), detail.dining(), detail.status(),
            detail.processingType(), detail.reason(), detail.createdAt(), detail.processedAt(), detail.processor());
    }
}
