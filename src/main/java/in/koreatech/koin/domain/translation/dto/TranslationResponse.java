package in.koreatech.koin.domain.translation.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.translation.model.TranslationReason;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record TranslationResponse<T>(
    @Schema(description = "번역 저장 결과/진행 작업 ID. 언어 판별·제공 불가·한국어 요약 원문 반환은 null입니다.", nullable = true)
    String translationId,
    @Schema(allowableValues = {"PENDING", "SUCCESS", "UNAVAILABLE", "FAILED"}, example = "SUCCESS")
    String status,
    @Schema(example = "en-US") String targetLanguage,
    @Schema(description = "실제 번역 대상 원문의 버전") String sourceVersion,
    @Schema(description = "실패 원인. MISSING_TRANSLATION: 누락, INVALID_TRANSLATION: 검증 실패, TIMEOUT: 시간 초과, CONFIGURATION_ERROR: 서버 설정 오류, UPSTREAM_REJECTED: Solar 요청 거절.", nullable = true)
    TranslationReason reasonCode,
    @Schema(description = "후속 POST 재시도 가능 여부. true이면 Retry-After를 따르고, false이면 자동 재시도하지 않습니다. SOURCE_NOT_READY는 원문 준비 후 요청합니다.") boolean retryable,
    @Schema(description = "SUCCESS인 경우 전체 번역 결과, 나머지 상태는 null", nullable = true) T data
) {
}
