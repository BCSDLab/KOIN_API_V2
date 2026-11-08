package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UserLanguageUpdateRequest(
    @Schema(description = "서버 선호값으로 동기화할 언어 코드. GET /languages의 코드를 사용하며 대소문자 차이는 정규화합니다.", example = "en-US", requiredMode = REQUIRED)
    @NotBlank(message = "언어 코드는 필수입니다.")
    String language
) {
}
