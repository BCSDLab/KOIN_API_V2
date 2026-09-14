package in.koreatech.koin.domain.teamrecruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
public record CreateChatMessageRequest(
        @Schema(description = "메시지 내용 (is_image=true이면 file_url)", example = "안녕하세요!", requiredMode = REQUIRED)
        @NotBlank
        String content,

        @Schema(description = "이미지 메시지 여부", example = "false", requiredMode = REQUIRED)
        @NotNull
        Boolean isImage
) {
}
