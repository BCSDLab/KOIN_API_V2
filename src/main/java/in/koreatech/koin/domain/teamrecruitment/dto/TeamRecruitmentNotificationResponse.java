package in.koreatech.koin.domain.teamrecruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record TeamRecruitmentNotificationResponse(
        @Schema(description = "알림 ID", example = "101", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "알림 타입", example = "NEW_APPLICATION", requiredMode = REQUIRED)
        String type,

        @Schema(description = "이동 대상 타입", example = "APPLICANT_MANAGEMENT", requiredMode = REQUIRED)
        String targetType,

        @Schema(description = "모집글 ID", example = "17", requiredMode = REQUIRED)
        Integer recruitmentId,

        @Schema(description = "지원서 ID. 지원서와 관련 없는 알림은 null입니다.", example = "51", nullable = true,
                requiredMode = REQUIRED)
        Integer applicationId,

        @Schema(description = "채팅방 ID. 채팅방과 관련 없는 알림은 null입니다.", example = "31", nullable = true,
                requiredMode = REQUIRED)
        Integer chatRoomId,

        @Schema(description = "발신자 닉네임. NEW_CHAT_MESSAGE가 아니면 null입니다.", example = "홍길동",
                nullable = true, requiredMode = REQUIRED)
        String senderNickname,

        @Schema(description = "메시지 미리보기", example = "새로운 지원자가 있어요.", requiredMode = REQUIRED)
        String messagePreview,

        @Schema(description = "읽음 여부", example = "false", requiredMode = REQUIRED)
        Boolean isRead,

        @Schema(description = "생성 일시 (KST LocalDateTime, ISO-8601)",
                example = "2026-08-26T11:20:30.123456", format = "date-time", requiredMode = REQUIRED)
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
