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
    @Schema(description = "서버가 번역 기준이 되는 원문 데이터를 SHA-256 방식으로 계산해 만든 64자리 문자열로, 같은 원문이면 같은 값이고 원문이 바뀌면 값도 바뀝니다. 원문 조회 응답의 translation_source_version을 그대로 복사해 보냅니다. 클라이언트에서 계산하지 않습니다. 기존 글·쪽지도 조회 시 버전이 자동으로 붙으므로, 기존 데이터에 버전값을 미리 채울 필요가 없습니다.")
    String expectedSourceVersion
) {
}
