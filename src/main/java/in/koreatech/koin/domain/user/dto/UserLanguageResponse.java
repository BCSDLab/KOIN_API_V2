package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserLanguageResponse(
    @Schema(description = "서버에 저장된 본인 선호 언어 코드. 미설정 시 ko-KR을 반환합니다. 기기 UI 언어를 자동 변경하는 값이 아닙니다.", example = "ko-KR", requiredMode = REQUIRED)
    String language
) {

    public static UserLanguageResponse from(SupportedLanguage language) {
        return new UserLanguageResponse(language.getCode());
    }
}
