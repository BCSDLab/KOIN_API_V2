package in.koreatech.koin.domain.translation.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.translation.dto.TranslationRequest;
import in.koreatech.koin.domain.translation.dto.TranslationResponse;
import in.koreatech.koin.domain.translation.model.StoredTranslation;
import in.koreatech.koin.domain.translation.model.TranslationReason;
import in.koreatech.koin.domain.translation.model.TranslationResource;
import in.koreatech.koin.domain.translation.model.TranslationStatus;
import in.koreatech.koin.domain.translation.model.TranslationType;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TranslationService {
    private final TranslationSourceReader sourceReader;
    private final TranslationStore store;
    private final TranslationAiClient aiClient;
    private final TranslationCapacity capacity;
    private final TranslationProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    // Solar 호출 중에는 DB 트랜잭션을 유지하지 않는다.
    public TranslationResponse<Map<String, Object>> translate(TranslationResource resource,
        TranslationRequest request, Integer userId) {
        SupportedLanguage language = SupportedLanguage.fromCode(request.targetLanguage());
        TranslationSource source = sourceReader.load(resource, userId);
        if (source.unavailableReason() != null) {
            return unavailable(source, language, source.unavailableReason());
        }
        assertVersion(source, request.expectedSourceVersion());
        if (source.texts().values().stream().mapToInt(String::length).sum() > properties.getMaxInputCharacters()) {
            return unavailable(source, language, TranslationReason.INPUT_TOO_LONG);
        }
        if (resource.type() == TranslationType.ARTICLE_SUMMARY && language == SupportedLanguage.KOREAN) {
            return new TranslationResponse<>(null, "SUCCESS", language.getCode(), source.version(), null, false, source.data());
        }

        TranslationStore.Claim claim = store.claim(resource, source.version(), language,
            properties.getModel(), properties.getPromptVersion());
        if (claim.ownerToken() == null) {
            return verifiedResponse(claim.translation(), userId);
        }
        String id = claim.translation().id();
        if (!capacity.tryAcquire()) {
            store.fail(id, claim.ownerToken(), TranslationReason.RATE_LIMITED, Duration.ofMinutes(1));
            return get(id, userId);
        }
        try {
            Map<String, String> translated = aiClient.translate(source.texts(), language);
            Map<String, Object> data = translatedData(source, translated);
            assertUsableVersion(sourceReader.load(resource, userId), source.version());
            store.succeed(id, claim.ownerToken(), objectMapper.writeValueAsString(data));
        } catch (TranslationFailureException e) {
            store.fail(id, claim.ownerToken(), e.getReason(), e.getRetryAfter());
        } catch (JsonProcessingException e) {
            store.fail(id, claim.ownerToken(), TranslationReason.INVALID_TRANSLATION, null);
        } catch (RuntimeException e) {
            // 권한·원문 변경은 원래 HTTP 오류로 반환하고 선점만 해제한다.
            store.fail(id, claim.ownerToken(), TranslationReason.TEMPORARY_ERROR, null);
            throw e;
        } finally {
            capacity.release();
        }
        // 다른 요청이 만료된 선점을 가져갔다면 그 요청의 현재 상태를 반환한다.
        return get(id, userId);
    }

    public TranslationResponse<Map<String, Object>> get(String id, Integer userId) {
        StoredTranslation stored = store.findById(id)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.TRANSLATION_NOT_FOUND));
        return verifiedResponse(stored, userId);
    }

    private TranslationResponse<Map<String, Object>> verifiedResponse(StoredTranslation stored, Integer userId) {
        if (!stored.expiresAt().isAfter(LocalDateTime.now(clock))) {
            throw CustomException.of(ApiResponseCode.TRANSLATION_NOT_FOUND);
        }
        TranslationSource current = sourceReader.load(stored.resource(), userId);
        assertUsableVersion(current, stored.sourceVersion());
        String status = stored.status().name();
        TranslationReason reason = stored.reason();
        if (stored.status() == TranslationStatus.PENDING
            && (stored.lockedUntil() == null || !stored.lockedUntil().isAfter(LocalDateTime.now(clock)))) {
            status = "FAILED";
            reason = TranslationReason.TEMPORARY_ERROR;
        }
        Map<String, Object> data = null;
        if (stored.status() == TranslationStatus.SUCCESS) {
            try {
                data = objectMapper.readValue(stored.translatedJson(), new TypeReference<>() {});
            } catch (JsonProcessingException | IllegalArgumentException e) {
                throw CustomException.of(ApiResponseCode.TRANSLATION_RESULT_INVALID);
            }
        }
        return new TranslationResponse<>(stored.id(), status, stored.targetLanguage(), stored.sourceVersion(),
            reason, "FAILED".equals(status), data);
    }

    public long retryAfterSeconds(TranslationResponse<?> response) {
        if ("PENDING".equals(response.status())) {
            return 2;
        }
        if (!"FAILED".equals(response.status()) || response.translationId() == null) {
            return 0;
        }
        return store.findById(response.translationId()).map(stored ->
            stored.nextAttemptAt() == null ? 1L
                : Math.max(1L, Duration.between(LocalDateTime.now(clock), stored.nextAttemptAt()).toSeconds() + 1))
            .orElse(1L);
    }

    private void assertUsableVersion(TranslationSource source, String expected) {
        if (source.unavailableReason() != null) {
            throw CustomException.of(ApiResponseCode.SOURCE_CHANGED);
        }
        assertVersion(source, expected);
    }

    private void assertVersion(TranslationSource source, String expected) {
        if (!Objects.equals(source.version(), expected)) {
            throw CustomException.of(ApiResponseCode.SOURCE_CHANGED);
        }
    }

    private TranslationResponse<Map<String, Object>> unavailable(TranslationSource source,
        SupportedLanguage language, TranslationReason reason) {
        return new TranslationResponse<>(null, "UNAVAILABLE", language.getCode(), source.version(), reason,
            reason == TranslationReason.SOURCE_NOT_READY, null);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> translatedData(TranslationSource source, Map<String, String> translated) {
        if (translated == null || !translated.keySet().containsAll(source.texts().keySet())
            || translated.values().stream().anyMatch(value -> !StringUtils.hasText(value))) {
            throw new TranslationFailureException(TranslationReason.MISSING_TRANSLATION, null);
        }
        if (!translated.keySet().equals(source.texts().keySet())) {
            throw new TranslationFailureException(TranslationReason.INVALID_TRANSLATION, null);
        }
        // 한국어 요약의 외국어 번역이 원문 그대로면 성공 결과로 저장하지 않는다.
        if (source.resource().type() == TranslationType.ARTICLE_SUMMARY && source.texts().equals(translated)) {
            throw new TranslationFailureException(TranslationReason.INVALID_TRANSLATION, null);
        }
        Map<String, Object> data = new LinkedHashMap<>(source.data());
        if (source.resource().type() == TranslationType.ARTICLE_SUMMARY) {
            List<Map<String, Object>> items = (List<Map<String, Object>>)source.data().get("items");
            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> item = new LinkedHashMap<>(items.get(i));
                String text = translated.get("item_" + i);
                if (text != null) {
                    item.put("text", text);
                }
                result.add(item);
            }
            data.put("items", result);
        } else if (source.resource().type() == TranslationType.DINING) {
            List<String> menu = (List<String>)source.data().get("menu");
            List<String> result = new ArrayList<>(menu);
            for (int i = 0; i < menu.size(); i++) {
                if (translated.containsKey("menu_" + i)) {
                    result.set(i, translated.get("menu_" + i));
                }
            }
            data.put("menu", result);
        } else if (source.resource().type() == TranslationType.ORDERABLE_SHOP_MENU) {
            data = translatedLabels(data, translated, "");
            List<Map<String, Object>> prices = (List<Map<String, Object>>)data.get("prices");
            List<Map<String, Object>> translatedPrices = new ArrayList<>();
            for (int i = 0; i < prices.size(); i++) {
                translatedPrices.add(translatedLabels(prices.get(i), translated, "price_" + i + "_"));
            }
            data.put("prices", translatedPrices);
            List<Map<String, Object>> groups = (List<Map<String, Object>>)data.get("option_groups");
            List<Map<String, Object>> translatedGroups = new ArrayList<>();
            for (int i = 0; i < groups.size(); i++) {
                String prefix = "group_" + i + "_";
                Map<String, Object> group = translatedLabels(groups.get(i), translated, prefix);
                List<Map<String, Object>> options = (List<Map<String, Object>>)group.get("options");
                List<Map<String, Object>> translatedOptions = new ArrayList<>();
                for (int j = 0; j < options.size(); j++) {
                    translatedOptions.add(translatedLabels(options.get(j), translated, prefix + "option_" + j + "_"));
                }
                group.put("options", translatedOptions);
                translatedGroups.add(group);
            }
            data.put("option_groups", translatedGroups);
        } else {
            data.putAll(translated);
        }
        return data;
    }

    private Map<String, Object> translatedLabels(Map<String, Object> source, Map<String, String> translated,
        String prefix) {
        Map<String, Object> result = new LinkedHashMap<>(source);
        for (String field : List.of("name", "description")) {
            String text = translated.get(prefix + field);
            if (text != null) {
                result.put(field, text);
            }
        }
        return result;
    }
}
