package in.koreatech.koin.domain.teamrecruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record ChatMessageResponse(
        @Schema(description = "메시지 ID", example = "901", requiredMode = REQUIRED)
        Integer messageId,

        @Schema(description = "발신자 유저 ID", example = "22", requiredMode = REQUIRED)
        Integer userId,

        @Schema(description = "발신자 닉네임", example = "김철수", requiredMode = REQUIRED)
        String userNickname,

        @Schema(description = "메시지 내용 (is_image=true이면 file_url)", example = "안녕하세요!", requiredMode = REQUIRED)
        String content,

        @Schema(description = "메시지 전송 시각 (KST LocalDateTime, ISO-8601)",
                example = "2026-08-26T11:20:30.123456", format = "date-time", requiredMode = REQUIRED)
        LocalDateTime timestamp,

        @Schema(description = "이미지 메시지 여부", example = "false", requiredMode = REQUIRED)
        Boolean isImage,

        @Schema(description = "아직 읽지 않은 다른 멤버 수", example = "2", requiredMode = REQUIRED)
        int unreadCount
) {
    public static ChatMessageResponse of(TeamRecruitmentChatMessage message, int unreadCount) {
        return new ChatMessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getSenderNickname(),
                message.getContent(),
                message.getCreatedAt(),
                message.getIsImage(),
                unreadCount
        );
    }
}
