package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.domain.dining.model.DiningReportProcessingType.MANUAL;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.model.DiningReport;
import in.koreatech.koin.domain.dining.model.DiningReportProcessingType;
import in.koreatech.koin.domain.dining.model.DiningReportStatus;
import in.koreatech.koin.domain.dining.model.DiningType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "삐봇용 제보 상세. 제보자 정보는 포함하지 않습니다.", example = """
    {
      "report_id": 1,
      "dining": {"id": 1, "date": "2026-10-02", "type": "LUNCH", "place": "A코너"},
      "status": "APPROVED",
      "processing_type": "MANUAL",
      "reason": "담당자 승인",
      "created_at": "2026-10-02 12:00:00",
      "processed_at": "2026-10-02 12:01:00",
      "processor": {"workspace_id": "T0123456789", "user_id": "U0123456789", "display_name": "홍길동"},
      "image_url": "https://static.koreatech.in/upload/COOP/2026/10/2/e924c7d3-3757-4cd0-961b-e65c1f6cc8ad/soldout.jpg",
      "processing_id": "550e8400-e29b-41d4-a716-446655440000",
      "source_report_id": null,
      "updated_at": "2026-10-02 12:01:00"
    }
    """)
public record DiningReportResponse(
    @Schema(description = "제보 ID", example = "1", requiredMode = REQUIRED)
    Integer reportId,
    @Schema(description = "제보 대상 식단", requiredMode = REQUIRED)
    DiningReference dining,
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
    DiningReportActor processor,
    @Schema(description = "접수한 공용 COOP 업로드의 원본 사진 URL",
        example = "https://static.koreatech.in/upload/COOP/2026/10/2/"
            + "e924c7d3-3757-4cd0-961b-e65c1f6cc8ad/soldout.jpg", format = "uri", requiredMode = REQUIRED)
    String imageUrl,
    @Schema(description = "함께 처리한 제보가 공유하는 처리 묶음 UUID. 대기 중이면 null입니다.",
        example = "550e8400-e29b-41d4-a716-446655440000", type = "string", format = "uuid",
        nullable = true, requiredMode = REQUIRED)
    UUID processingId,
    @Schema(description = "같은 식단의 자동 승인을 발생시킨 제보 ID. SAME_DINING_APPROVED일 때만 값이 있고 그 외에는 null입니다.",
        example = "1", nullable = true, requiredMode = REQUIRED)
    Integer sourceReportId,
    @Schema(description = "마지막 상태 변경 시각 (한국시간)", example = "2026-10-02 12:01:00",
        implementation = String.class, type = "string", pattern = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$",
        requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
) {
    public static DiningReportResponse from(DiningReport report) {
        boolean manual = report.getProcessingType() == MANUAL;
        DiningReportActor actor = manual
            ? new DiningReportActor(report.getProcessorWorkspaceId(), report.getProcessorUserId(),
                report.getProcessorName()) : null;
        return new DiningReportResponse(
            report.getId(), DiningReference.from(report.getDining()), report.getStatus(),
            report.getProcessingType(), report.getReason(), report.getCreatedAt(),
            manual ? report.getProcessedAt() : null, actor, report.getImageUrl(), report.getProcessingId(),
            report.getSourceReportId(), report.getUpdatedAt()
        );
    }

    public record DiningReference(
        @Schema(description = "식단 ID", example = "1", requiredMode = REQUIRED)
        Integer id,
        @Schema(description = "식단 날짜 (yyyy-MM-dd)", example = "2026-10-02",
            type = "string", format = "date", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @Schema(description = "식사 구분. BREAKFAST는 아침, LUNCH는 점심, DINNER는 저녁입니다.",
            example = "LUNCH", requiredMode = REQUIRED)
        DiningType type,
        @Schema(description = "식단 코스명", example = "A코너", requiredMode = REQUIRED)
        String place
    ) {
        public static DiningReference from(Dining dining) {
            return new DiningReference(dining.getId(), dining.getDate(), dining.getType(), dining.getPlace());
        }
    }
}
