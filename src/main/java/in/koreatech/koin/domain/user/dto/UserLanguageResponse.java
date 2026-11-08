package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserLanguageResponse(
    @Schema(description = "계정의 언어 코드. 미설정 시 ko-KR을 반환합니다.", example = "ko-KR", requiredMode = REQUIRED)
    String language
) {

    public static UserLanguageResponse from(SupportedLanguage language) {
        return new UserLanguageResponse(language.getCode());
    }
}
