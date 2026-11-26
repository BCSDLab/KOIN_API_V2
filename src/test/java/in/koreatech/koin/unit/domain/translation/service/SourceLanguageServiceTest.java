package in.koreatech.koin.unit.domain.translation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import in.koreatech.koin.domain.translation.dto.SourceLanguageRequest;
import in.koreatech.koin.domain.translation.model.TranslationType;
import in.koreatech.koin.domain.translation.model.TranslationReason;
import in.koreatech.koin.domain.translation.service.*;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

class SourceLanguageServiceTest {
    @Test
    @SuppressWarnings("unchecked")
    void 실제_원문으로_판별하고_캐시가_있어도_접근권한을_검사한다() {
        var reader = mock(TranslationSourceReader.class);
        var ai = mock(TranslationAiClient.class);
        var redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        var properties = new TranslationProperties();
        var service = new SourceLanguageService(reader, ai, new TranslationCapacity(properties, Clock.systemUTC()),
            properties, redis);
        var request = new SourceLanguageRequest(TranslationType.RECRUITMENT_MESSAGE, "12", 1, 2, "en-US");
        var texts = Map.of("content", "Ertaga uchrashamiz.");
        var source = TranslationSource.of(request.resource(), Map.of("content", texts.get("content")), texts, null);
        when(reader.load(request.resource(), 7)).thenReturn(source);
        when(ai.detectLanguage(texts)).thenReturn("uz-Latn");
        assertThat(service.detect(request, 7).data().translationRequired()).isTrue();
        verify(ai).detectLanguage(texts);

        when(values.get(anyString())).thenReturn("uz-Latn");
        var sameLanguage = new SourceLanguageRequest(TranslationType.RECRUITMENT_MESSAGE, "12", 1, 2, "uz-Latn");
        assertThat(service.detect(sameLanguage, 7).data().translationRequired()).isFalse();
        verifyNoMoreInteractions(ai);
        when(reader.load(request.resource(), 7)).thenReturn(TranslationSource.of(request.resource(),
            Map.of("content", "123"), Map.of(), TranslationReason.UNSUPPORTED_CONTENT));
        assertThat(service.detect(request, 7).data().translationRequired()).isFalse();
        when(reader.load(request.resource(), 8)).thenThrow(CustomException.of(ApiResponseCode.TEAM_RECRUITMENT_CHAT_FORBIDDEN));
        assertThatThrownBy(() -> service.detect(request, 8)).isInstanceOf(CustomException.class);
        verifyNoMoreInteractions(ai);
    }
}
