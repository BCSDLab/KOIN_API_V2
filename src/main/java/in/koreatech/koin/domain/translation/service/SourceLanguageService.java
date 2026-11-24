package in.koreatech.koin.domain.translation.service;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.translation.dto.SourceLanguageData;
import in.koreatech.koin.domain.translation.dto.SourceLanguageRequest;
import in.koreatech.koin.domain.translation.dto.TranslationResponse;
import in.koreatech.koin.domain.translation.model.TranslationReason;
import in.koreatech.koin.domain.translation.model.TranslationType;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SourceLanguageService {
    private final TranslationSourceReader sourceReader;
    private final TranslationAiClient aiClient;
    private final TranslationCapacity capacity;
    private final TranslationProperties properties;
    private final StringRedisTemplate redis;

    public TranslationResponse<SourceLanguageData> detect(SourceLanguageRequest request, Integer userId) {
        SupportedLanguage target = SupportedLanguage.fromCode(request.targetLanguage());
        // 캐시가 있어도 원문 생존·현재 접근 권한부터 확인한다.
        TranslationSource source = sourceReader.load(request.resource(), userId);
        if (source.unavailableReason() == TranslationReason.UNSUPPORTED_CONTENT) {
            return success(source, target, "und");
        }
        if (source.unavailableReason() != null) {
            return failed(source, target, source.unavailableReason());
        }
        if (source.texts().values().stream().mapToLong(String::length).sum() > properties.getMaxInputCharacters()) {
            return failed(source, target, TranslationReason.INPUT_TOO_LONG);
        }
        if (source.resource().type() == TranslationType.ARTICLE_SUMMARY) {
            return success(source, target, SupportedLanguage.KOREAN.getCode());
        }
        if (source.texts().values().stream().noneMatch(text -> text.codePoints().anyMatch(Character::isLetter))) {
            return success(source, target, "und");
        }
        String key = "translation:source-language:" + TranslationSourceVersion.of(Map.of(
            "texts", source.texts(), "model", properties.getModel(), "prompt", "language-v1"));
        String cached = cachedLanguage(key);
        if (cached != null) {
            return success(source, target, cached);
        }
        if (!capacity.tryAcquire()) {
            return failed(source, target, TranslationReason.RATE_LIMITED);
        }
        try {
            String detected = aiClient.detectLanguage(source.texts());
            TranslationSource current = sourceReader.load(request.resource(), userId);
            if (current.unavailableReason() != null || !Objects.equals(source.version(), current.version())) {
                throw CustomException.of(ApiResponseCode.SOURCE_CHANGED);
            }
            cacheLanguage(key, detected);
            return success(source, target, detected);
        } finally {
            capacity.release();
        }
    }

    private TranslationResponse<SourceLanguageData> success(TranslationSource source, SupportedLanguage target,
        String language) {
        return new TranslationResponse<>(null, "SUCCESS", target.getCode(), source.version(), null, false,
            new SourceLanguageData(language, !"und".equals(language) && !target.getCode().equals(language)));
    }

    private TranslationResponse<SourceLanguageData> failed(TranslationSource source, SupportedLanguage target,
        TranslationReason reason) {
        return new TranslationResponse<>(null, "FAILED", target.getCode(), source.version(), reason,
            reason.isRetryable(), null);
    }

    private String cachedLanguage(String key) {
        try {
            return redis.opsForValue().get(key);
        } catch (DataAccessException e) {
            return null; // 캐시 장애는 언어 판별 자체를 막지 않는다.
        }
    }

    private void cacheLanguage(String key, String language) {
        try {
            // 원문은 저장하지 않고 버전에 대응하는 언어 코드만 짧게 보관한다.
            redis.opsForValue().set(key, language, Duration.ofDays(1));
        } catch (DataAccessException ignored) {
            // 다음 요청에서 다시 판별할 수 있다.
        }
    }
}
