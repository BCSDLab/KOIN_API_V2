package in.koreatech.koin.admin.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dining.dto.DiningReportActor;
import in.koreatech.koin.domain.dining.dto.DiningReportResponse;
import in.koreatech.koin.domain.dining.model.DiningReport;
import in.koreatech.koin.domain.dining.model.DiningReportProcessingType;
import in.koreatech.koin.domain.dining.model.DiningReportStatus;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "관리자 조회용 제보 상세. 제보자 정보를 함께 반환합니다.", example = """
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
      "updated_at": "2026-10-02 12:01:00",
      "reporter": {"id": 10, "name": "김코인"}
    }
    """)
public record AdminDiningReportResponse(
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt,
    @Schema(description = "제보자 정보. 사용자 정보를 조회할 수 없으면 null입니다.", nullable = true, requiredMode = REQUIRED)
    Reporter reporter
) {
    public static AdminDiningReportResponse from(DiningReport report, User user) {
        DiningReportResponse detail = DiningReportResponse.from(report);
        Reporter reporter = user == null ? null : new Reporter(user.getId(),
            StringUtils.hasText(user.getName()) ? user.getName()
                : StringUtils.hasText(user.getNickname()) ? user.getNickname() : "회원");
        return new AdminDiningReportResponse(detail.reportId(), detail.dining(), detail.status(),
            detail.processingType(), detail.reason(), detail.createdAt(), detail.processedAt(), detail.processor(),
            detail.imageUrl(), detail.processingId(), detail.sourceReportId(), detail.updatedAt(), reporter);
    }

    public record Reporter(
        @Schema(description = "제보자 사용자 ID", example = "10", requiredMode = REQUIRED)
        Integer id,
        @Schema(description = "제보자 이름. 이름이 없으면 닉네임 또는 회원을 반환합니다.", example = "김코인", requiredMode = REQUIRED)
        String name
    ) {
    }
}
