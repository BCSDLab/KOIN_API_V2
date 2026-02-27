package in.koreatech.koin.domain.community.lostitem.chatmessage.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ChatMessageRequest(
    @Schema(description = "메시지 전송 사용자 닉네임", example = "코인")
    @NotBlank(message = "닉네임은 필수입니다")
    String userNickname,

    @Schema(description = "메시지 본문 혹은 이미지 url", example = "안녕하세요")
    @NotBlank(message = "메시지 내용은 필수입니다")
    String content,

    @Schema(description = "이미지 메시지 여부", example = "false")
    @NotNull(message = "이미지 여부는 필수입니다")
    Boolean isImage
) {

    public ChatMessage toChatMessage(Integer userId) {
        return new ChatMessage(userId, this.userNickname, this.content, LocalDateTime.now(), this.isImage);
    }
}
