package in.koreatech.koin.domain.teamrecruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record TeamRecruitmentNotificationsResponse(
        @Schema(description = "알림 목록", requiredMode = REQUIRED)
        List<TeamRecruitmentNotificationResponse> notifications,

        @Schema(description = "전체 미읽음 알림 수", example = "3", requiredMode = REQUIRED)
        long unreadCount,

        @Schema(description = "전체 알림 수", example = "24", requiredMode = REQUIRED)
        long totalCount,

        @Schema(description = "현재 페이지 알림 수", example = "10", requiredMode = REQUIRED)
        int currentCount,

        @Schema(description = "전체 페이지 수", example = "3", requiredMode = REQUIRED)
        int totalPage,

        @Schema(description = "현재 페이지 (1부터 시작)", example = "1", requiredMode = REQUIRED)
        int currentPage
) {
}
