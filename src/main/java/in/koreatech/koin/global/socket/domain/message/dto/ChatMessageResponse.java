package in.koreatech.koin.global.socket.domain.message.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.global.socket.domain.message.model.ChatMessageCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ChatMessageResponse(
    @Schema(description = "메시지 전송 사용자 ID", example = "5665")
    Integer userId,
    @Schema(description = "메시지 전송 사용자 닉네임", example = "코인")
    String userNickname,
    @Schema(description = "메시지 본문 혹은 이미지 url", example = "안녕하세요")
    String content,
    @Schema(description = "메시지 전송 시간", example = "2025-01-18T20:08:10.6441253")
    LocalDateTime timestamp,
    @Schema(description = "이미지 메시지 여부", example = "false")
    Boolean isImage
) {

    public static ChatMessageResponse toResponse(ChatMessageCommand message) {
        return new ChatMessageResponse(
            message.userId(),
            message.userNickname(),
            message.content(),
            message.timestamp(),
            message.isImage()
        );
    }
}
