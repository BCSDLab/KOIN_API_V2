package in.koreatech.koin.domain.translation.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.translation.model.TranslationReason;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record TranslationResponse<T>(
    @Schema(description = "저장 결과/진행 작업 ID. 제공 불가 또는 한국어 요약 원문 반환은 null입니다.", nullable = true)
    String translationId,
    @Schema(allowableValues = {"PENDING", "SUCCESS", "UNAVAILABLE", "FAILED"}, example = "SUCCESS")
    String status,
    @Schema(example = "en-US") String targetLanguage,
    @Schema(description = "실제 번역 대상 원문의 버전") String sourceVersion,
    @Schema(nullable = true) TranslationReason reasonCode,
    @Schema(description = "재시도 가능 여부. 대기 시간은 Retry-After를 따릅니다.") boolean retryable,
    @Schema(description = "SUCCESS인 경우 전체 번역 결과, 나머지 상태는 null", nullable = true) T data
) {
}
