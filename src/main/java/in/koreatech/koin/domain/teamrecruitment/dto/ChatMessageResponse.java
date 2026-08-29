package in.koreatech.koin.domain.teamrecruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.teamrecruitment.model.TeamRecruitmentChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record ChatMessageResponse(
        @Schema(description = "메시지 ID", example = "901")
        Integer messageId,

        @Schema(description = "발신자 유저 ID", example = "22")
        Integer userId,

        @Schema(description = "발신자 닉네임", example = "김철수")
        String userNickname,

        @Schema(description = "메시지 내용 (is_image=true이면 file_url)", example = "안녕하세요!")
        String content,

        @Schema(description = "메시지 전송 시각 (KST LocalDateTime)", example = "2026-08-26T11:20:30.123456")
        LocalDateTime timestamp,

        @Schema(description = "이미지 메시지 여부", example = "false")
        Boolean isImage,

        @Schema(description = "아직 읽지 않은 다른 멤버 수", example = "2")
        int unreadCount
) {
    public static ChatMessageResponse of(TeamRecruitmentChatMessage message, int unreadCount) {
        return new ChatMessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getContent(),
                message.getCreatedAt(),
                message.getIsImage(),
                unreadCount
        );
    }
}
