package in.koreatech.koin.domain.chat.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.translation.service.TranslationSourceFields;
import in.koreatech.koin.domain.translation.service.TranslationSourceVersion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@JsonNaming(SnakeCaseStrategy.class)
@Schema(name = "CommonChatMessageResponse")
public record ChatMessageResponse(
    @Schema(description = "메시지 ID. TSID 정밀도를 보존하기 위해 문자열로 반환합니다.", example = "901")
    String messageId,
    Integer userId,
    String userNickname,
    String content,
    LocalDateTime timestamp,
    Boolean isImage,
    @Schema(description = "이 메시지를 아직 읽지 않은 다른 참여자 수")
    Integer unreadCount,
    boolean isLeftUser
) {
    @JsonProperty(value = "translation_source_version", access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "번역 요청의 expected_source_version으로 전달할 원문 버전. 이미지 메시지는 null입니다.", nullable = true)
    public String getTranslationSourceVersion() {
        return Boolean.TRUE.equals(isImage) ? null
            : TranslationSourceVersion.of(TranslationSourceFields.message(content));
    }
}
