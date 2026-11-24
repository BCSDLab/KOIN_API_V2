package in.koreatech.koin.domain.translation.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record SourceLanguageData(
    @Schema(description = "실제 원문 언어. other=그 외 언어, mul=여러 언어, und=판별 불가", example = "ko-KR")
    String sourceLanguage,
    @Schema(description = "true이면 번역 버튼을 표시합니다. 같은 언어 또는 판별 가능한 문장이 없으면 false")
    boolean translationRequired
) {
}
