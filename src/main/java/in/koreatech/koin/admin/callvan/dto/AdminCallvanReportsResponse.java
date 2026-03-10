package in.koreatech.koin.admin.callvan.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.callvan.model.CallvanReport;
import in.koreatech.koin.domain.callvan.model.CallvanReportAttachment;
import in.koreatech.koin.domain.callvan.model.CallvanReportProcess;
import in.koreatech.koin.domain.callvan.model.CallvanReportReason;
import in.koreatech.koin.domain.callvan.model.enums.CallvanReportProcessType;
import in.koreatech.koin.domain.callvan.model.enums.CallvanReportReasonCode;
import in.koreatech.koin.domain.callvan.model.enums.CallvanReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminCallvanReportsResponse(
    @Schema(description = "신고 접수 목록", requiredMode = REQUIRED)
    List<AdminCallvanReportResponse> reports,

    @Schema(description = "전체 신고 수", example = "12", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "현재 개수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "전체 페이지", example = "2", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "1", requiredMode = REQUIRED)
    Integer currentPage
) {

    public static AdminCallvanReportsResponse from(
        CallvanReportPagedResult pagedResult,
        CallvanReportRelatedData relatedData) {
        List<AdminCallvanReportResponse> responses = pagedResult.reports().stream()
            .map(report -> AdminCallvanReportResponse.of(report, relatedData))
            .toList();

        return new AdminCallvanReportsResponse(
            responses,
            pagedResult.totalCount(),
            responses.size(),
            pagedResult.totalPages(),
            pagedResult.currentPage());
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record AdminCallvanReportResponse(
        @Schema(description = "신고 ID", example = "1", requiredMode = REQUIRED)
        Integer reportId,

        @Schema(description = "신고 상태", example = "PENDING", requiredMode = REQUIRED)
        CallvanReportStatus reportStatus,

        @Schema(description = "피신고자 정보", requiredMode = REQUIRED)
        ReportedUserResponse reportedUser,

        @Schema(description = "신고 접수 일자", example = "2026-03-07T13:20:00", requiredMode = REQUIRED)
        LocalDateTime reportedAt,

        @Schema(description = "신고 사유", requiredMode = REQUIRED)
        List<ReasonResponse> reasons,

        @Schema(description = "어드민 처리 상태")
        CallvanReportProcessType processType,

        @Schema(description = "임시 차단 종료일")
        LocalDateTime restrictedUntil,

        @Schema(description = "신고 상황")
        String description,

        @Schema(description = "첨부파일(이미지) url")
        List<String> attachmentUrls,

        @Schema(description = "피신고자에 대한 누적 신고 건수", example = "3", requiredMode = REQUIRED)
        Integer accumulatedReportCount,

        @Schema(description = "누적 신고 정보", requiredMode = REQUIRED)
        List<AccumulatedReportHistoryResponse> accumulatedReports
    ) {

        public static AdminCallvanReportResponse of(CallvanReport report, CallvanReportRelatedData relatedData) {
            CallvanReportProcess process = relatedData.process(report.getId());

            List<AccumulatedReportHistoryResponse> accumulatedHistories = relatedData
                .accumulatedReports(report.getReported().getId()).stream()
                .filter(history -> !history.getId().equals(report.getId()))
                .map(history -> AccumulatedReportHistoryResponse.of(history, relatedData))
                .toList();

            return new AdminCallvanReportResponse(
                report.getId(),
                report.getStatus(),
                ReportedUserResponse.from(report),
                report.getCreatedAt(),
                relatedData.reasons(report.getId()).stream().map(ReasonResponse::from).toList(),
                process != null ? process.getProcessType() : null,
                process != null ? process.getRestrictedUntil() : null,
                report.getDescription(),
                relatedData.attachments(report.getId()).stream()
                    .map(CallvanReportAttachment::getUrl).toList(),
                accumulatedHistories.size() + 1,
                accumulatedHistories
            );
        }
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record ReportedUserResponse(
        @Schema(description = "id", example = "23", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "사용자 이름", example = "홍길동")
        String name,

        @Schema(description = "사용자 닉네임", example = "코리")
        String nickname
    ) {

        public static ReportedUserResponse from(CallvanReport report) {
            return new ReportedUserResponse(
                report.getReported().getId(),
                report.getReported().getName(),
                report.getReported().getNickname());
        }
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record ReasonResponse(
        @Schema(description = "신고 사유", example = "NON_PAYMENT", requiredMode = REQUIRED)
        CallvanReportReasonCode reasonCode,

        @Schema(description = "신고 사유(기타) 출력")
        String customText
    ) {

        public static ReasonResponse from(CallvanReportReason reason) {
            return new ReasonResponse(reason.getReasonCode(), reason.getCustomText());
        }
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record AccumulatedReportHistoryResponse(
        @Schema(description = "Id", example = "1", requiredMode = REQUIRED)
        Integer reportId,

        @Schema(description = "신고 접수 일자", example = "2026-03-07T13:20:00", requiredMode = REQUIRED)
        LocalDateTime reportedAt,

        @Schema(description = "신고 처리 상태", example = "CONFIRMED", requiredMode = REQUIRED)
        CallvanReportStatus reportStatus,

        @Schema(description = "어드민 처리 유형")
        CallvanReportProcessType processType,

        @Schema(description = "임시 차단 종료일")
        LocalDateTime restrictedUntil,

        @Schema(description = "신고 사유", requiredMode = REQUIRED)
        List<ReasonResponse> reasons
    ) {

        public static AccumulatedReportHistoryResponse of(CallvanReport report, CallvanReportRelatedData relatedData) {
            CallvanReportProcess process = relatedData.process(report.getId());

            return new AccumulatedReportHistoryResponse(
                report.getId(),
                report.getCreatedAt(),
                report.getStatus(),
                process != null ? process.getProcessType() : null,
                process != null ? process.getRestrictedUntil() : null,
                relatedData.reasons(report.getId()).stream().map(ReasonResponse::from).toList());
        }
    }
}
