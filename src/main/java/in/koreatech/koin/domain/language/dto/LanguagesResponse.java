package in.koreatech.koin.domain.language.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LanguagesResponse(
    @Schema(description = "서버 기본 언어 코드. 기기의 기존 UI 선택을 덮어쓰는 값이 아니며 사용자별 선택값은 포함하지 않습니다.", example = "ko-KR", requiredMode = REQUIRED)
    String defaultLanguage,

    @Schema(description = "화면 표시 순서의 지원 언어 목록", requiredMode = REQUIRED)
    List<LanguageResponse> languages
) {

    public static LanguagesResponse from() {
        return new LanguagesResponse(
            SupportedLanguage.DEFAULT.getCode(),
            Arrays.stream(SupportedLanguage.values())
                .map(language -> new LanguageResponse(language.getCode(), language.getName()))
                .toList()
        );
    }

    public record LanguageResponse(
        @Schema(description = "기기 UI 선택, 서버 선호값 동기화와 번역 요청에 사용할 언어 코드", example = "en-US", requiredMode = REQUIRED)
        String code,

        @Schema(description = "현재 화면 언어와 관계없는 해당 언어의 고유 표기 이름", example = "English", requiredMode = REQUIRED)
        String name
    ) {
    }
}
