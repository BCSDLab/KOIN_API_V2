package in.koreatech.koin.domain.callvan.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
public record CallvanChatMessageRequest(
    @Schema(description = "메시지 내용 (텍스트 또는 이미지 URL)", example = "안녕하세요!")
    @NotNull(message = "메시지 내용은 필수입니다.")
    String content,

    @Schema(description = "이미지 여부", example = "false")
    @JsonProperty("is_image")
    @NotNull(message = "이미지 여부는 필수입니다.")
    Boolean isImage
) {

}
