package in.koreatech.koin.domain.translation.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonNaming(SnakeCaseStrategy.class)
public record TranslationRequest(
    @NotBlank @Schema(description = "현재 화면 언어. GET /languages의 code를 사용합니다.", example = "en-US")
    String targetLanguage,
    @NotBlank @Pattern(regexp = "[a-f0-9]{64}", message = "원문 버전은 64자리 SHA-256 값이어야 합니다.")
    @Schema(description = "원문 응답의 translation_source_version. 공지 요약은 ai_summary 안의 값을 사용합니다.")
    String expectedSourceVersion
) {
}
