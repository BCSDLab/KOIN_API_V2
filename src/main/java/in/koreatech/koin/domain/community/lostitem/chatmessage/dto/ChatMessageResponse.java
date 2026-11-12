package in.koreatech.koin.domain.community.lostitem.chatmessage.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.annotation.JsonProperty;

import in.koreatech.koin.domain.translation.service.TranslationSourceFields;
import in.koreatech.koin.domain.translation.service.TranslationSourceVersion;

import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
@Schema(name = "LostItemChatMessageResponse")
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
    Boolean isImage,
    @Schema(description = "저장된 메시지 ID. JavaScript 정밀도 손실을 막기 위해 문자열로 제공합니다.", example = "733900000000012345")
    String messageId
) {

    @JsonProperty("translation_source_version")
    @Schema(description = "현재 텍스트 메시지의 번역 원문 버전. 이미지 메시지는 null입니다.", nullable = true)
    public String getTranslationSourceVersion() {
        return Boolean.TRUE.equals(isImage) ? null
            : TranslationSourceVersion.of(TranslationSourceFields.message(content));
    }

    public static ChatMessageResponse toResponse(ChatMessageEntity entity) {
        return new ChatMessageResponse(
            entity.getUserId(),
            entity.getNickName(),
            entity.getContents(),
            entity.getCreatedAt(),
            entity.getIsImage(),
            entity.getId().toString()
        );
    }
}
