package in.koreatech.koin.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
@Schema(name = "CommonCreateChatMessageRequest")
public record CreateChatMessageRequest(
    @NotBlank(message = "메시지 내용은 필수입니다.")
    String content,
    @JsonProperty("is_image")
    @NotNull(message = "이미지 여부는 필수입니다.")
    Boolean isImage
) {
}
