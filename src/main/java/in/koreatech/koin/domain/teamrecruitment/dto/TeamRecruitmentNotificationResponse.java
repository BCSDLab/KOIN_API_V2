package in.koreatech.koin.domain.teamrecruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record TeamRecruitmentNotificationResponse(
        @Schema(description = "알림 ID", example = "101")
        Integer id,

        @Schema(description = "알림 타입", example = "NEW_APPLICATION")
        String type,

        @Schema(description = "이동 대상 타입", example = "APPLICANT_MANAGEMENT")
        String targetType,

        @Schema(description = "모집글 ID", example = "17")
        Integer recruitmentId,

        @Schema(description = "지원서 ID", example = "51")
        Integer applicationId,

        @Schema(description = "채팅방 ID", example = "31")
        Integer chatRoomId,

        @Schema(description = "발신자 닉네임 (NEW_CHAT_MESSAGE 전용)", example = "홍길동")
        String senderNickname,

        @Schema(description = "메시지 미리보기", example = "새로운 지원자가 있어요.")
        String messagePreview,

        @Schema(description = "읽음 여부", example = "false")
        Boolean isRead,

        @Schema(description = "생성 일시")
        LocalDateTime createdAt
) {
    public static TeamRecruitmentNotificationResponse from(TeamRecruitmentNotification notification) {
        return new TeamRecruitmentNotificationResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getTargetType().name(),
                notification.getRecruitment().getId(),
                notification.getApplication() != null ? notification.getApplication().getId() : null,
                notification.getChatRoom() != null ? notification.getChatRoom().getId() : null,
                notification.getSenderNickname(),
                notification.getMessagePreview(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
