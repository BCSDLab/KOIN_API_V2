package in.koreatech.koin.socket.domain.message.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 클라이언트에서 전달 되고, 클라이언트에 전달되는 메시지 객체
 */
@Builder
@Schema(description = "채팅 메시지 객체")
@JsonNaming(value = SnakeCaseStrategy.class)
public record ChatMessageCommand(
    @Schema(description = "user 테이블 PK")
    Integer userId,

    @Schema(description = "체팅방에 표시되는 user 이름")
    String userNickname,

    @Schema(description = "채팅 메시지 혹은 이미지 url")
    String content,

    @Schema(description = "시간")
    LocalDateTime timestamp,

    @Schema(description = "이미지 url 여부")
    Boolean isImage
) {
    public static ChatMessageCommand toCommand(ChatMessageEntity message) {
        return new ChatMessageCommand(
            message.getUserId(),
            message.getNickName(),
            message.getContents(),
            message.getCreatedAt(),
            message.getIsImage()
        );
    }
}
