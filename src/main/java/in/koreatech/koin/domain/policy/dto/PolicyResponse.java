package in.koreatech.koin.domain.policy.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record PolicyResponse(
    @Schema(allowableValues = {"terms", "privacy"}) String type,
    @Schema(description = "요청 언어", example = "en-US") String requestedLanguage,
    @Schema(description = "실제 문서 언어", example = "ko-KR") String language,
    @Schema(description = "검수된 요청 언어 문서가 없어 한국어 원문을 제공했는지 여부") boolean isFallback,
    @Schema(description = "원문 순서의 조항 목록. 각 항목은 id, title, content(문자열 또는 문자열 배열), 선택적으로 items, additionalContent, additionalItems를 포함합니다.", type = "array")
    JsonNode sections
) {}
