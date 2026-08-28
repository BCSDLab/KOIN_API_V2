package in.koreatech.koin.domain.teamrecruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.teamrecruitment.model.TeamRecruitmentNotification;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record TeamRecruitmentNotificationResponse(
        @Schema(description = "알림 ID", example = "1")
        Integer id,

        @Schema(description = "알림 타입", example = "NEW_APPLICATION")
        String notificationType,

        @Schema(description = "모집글 ID", example = "1")
        Integer recruitmentId,

        @Schema(description = "지원서 ID", example = "1")
        Integer applicationId,

        @Schema(description = "발신자 닉네임", example = "홍길동")
        String senderNickname,

        @Schema(description = "메시지 미리보기", example = "안녕하세요!")
        String messagePreview,

        @Schema(description = "읽음 여부", example = "false")
        Boolean isRead,

        @Schema(description = "생성 일시")
        LocalDateTime createdAt
) {
    public static TeamRecruitmentNotificationResponse from(TeamRecruitmentNotification notification) {
        return new TeamRecruitmentNotificationResponse(
                notification.getId(),
                notification.getNotificationType().name(),
                notification.getRecruitmentId(),
                notification.getApplicationId(),
                notification.getSenderNickname(),
                notification.getMessagePreview(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
