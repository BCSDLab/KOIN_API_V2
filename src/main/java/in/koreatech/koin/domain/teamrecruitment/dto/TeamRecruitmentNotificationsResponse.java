package in.koreatech.koin.domain.teamrecruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record TeamRecruitmentNotificationsResponse(
        @Schema(description = "알림 목록")
        List<TeamRecruitmentNotificationResponse> notifications,

        @Schema(description = "현재 페이지")
        int page,

        @Schema(description = "전체 페이지 수")
        int totalPages,

        @Schema(description = "전체 미읽음 알림 수")
        long unreadCount
) {
}
