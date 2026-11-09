package in.koreatech.koin.unit.domain.translation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.translation.dto.TranslationRequest;
import in.koreatech.koin.domain.translation.model.StoredTranslation;
import in.koreatech.koin.domain.translation.model.TranslationReason;
import in.koreatech.koin.domain.translation.model.TranslationResource;
import in.koreatech.koin.domain.translation.model.TranslationStatus;
import in.koreatech.koin.domain.translation.model.TranslationType;
import in.koreatech.koin.domain.translation.service.*;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class TranslationServiceTest {
    private static final String ID = "translation-id";
    private static final String OWNER = "owner-token";
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-09T00:00:00Z"), ZoneOffset.UTC);
    private final TranslationProperties properties = new TranslationProperties();
    private final ObjectMapper mapper = new ObjectMapper();
    private final TranslationResource resource = TranslationResource.article(TranslationType.LOST_ITEM, 10);
    private final TranslationSource source = TranslationSource.of(resource,
        TranslationSourceFields.lostItem("학생회관", null), Map.of("found_place", "학생회관"), null);
    @Mock TranslationSourceReader reader;
    @Mock TranslationStore store;
    @Mock TranslationAiClient ai;
    @Mock TranslationCapacity capacity;
    private TranslationService service;

    @BeforeEach
    void setUp() {
        service = new TranslationService(reader, store, ai, capacity, properties, mapper, clock);
    }

    @Test
    void successfulTranslationPreservesNullAndIsReadBackFromStorage() throws Exception {
        prepareOwner();
        when(ai.translate(source.texts(), SupportedLanguage.ENGLISH)).thenReturn(Map.of("found_place", "Student Union"));
        String result = "{\"found_place\":\"Student Union\",\"content\":null}";
        when(store.findById(ID)).thenReturn(Optional.of(stored(TranslationStatus.SUCCESS, result)));

        var response = service.translate(resource, request(), null);

        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.targetLanguage()).isEqualTo("en-US");
        assertThat(response.data()).containsEntry("found_place", "Student Union").containsEntry("content", null);
        verify(store).succeed(ID, OWNER, result);
        verify(capacity).release();
    }

    @Test
    void cachedSuccessRevalidatesSourceWithoutAiOrCapacity() {
        when(reader.load(resource, null)).thenReturn(source);
        when(store.claim(any(), anyString(), any(), anyString(), anyString())).thenReturn(
            new TranslationStore.Claim(stored(TranslationStatus.SUCCESS, "{\"found_place\":\"Student Union\",\"content\":null}"), null));
        assertThat(service.translate(resource, request(), null).status()).isEqualTo("SUCCESS");
        verify(reader, times(2)).load(resource, null);
        verifyNoInteractions(ai, capacity);
    }

    @Test
    void duplicatePendingDoesNotCallAiAndProvidesPollingDelay() {
        when(reader.load(resource, null)).thenReturn(source);
        when(store.claim(any(), anyString(), any(), anyString(), anyString())).thenReturn(
            new TranslationStore.Claim(stored(TranslationStatus.PENDING, null), null));
        var response = service.translate(resource, request(), null);
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(service.retryAfterSeconds(response)).isEqualTo(2);
        verifyNoInteractions(ai, capacity);
    }

    @Test
    void staleClientVersionIsRejectedBeforeClaimOrAi() {
        when(reader.load(resource, null)).thenReturn(source);
        assertThatThrownBy(() -> service.translate(resource, new TranslationRequest("en-US", "0".repeat(64)), null))
            .isInstanceOfSatisfying(CustomException.class, error -> assertThat(error.getErrorCode()).isEqualTo(ApiResponseCode.SOURCE_CHANGED));
        verifyNoInteractions(store, ai, capacity);
    }

    @Test
    void sourceChangeDuringSolarDiscardsResultAndReleasesCapacity() {
        prepareOwner();
        var changed = TranslationSource.of(resource, TranslationSourceFields.lostItem("도서관", null),
            Map.of("found_place", "도서관"), null);
        when(reader.load(resource, null)).thenReturn(source, changed);
        when(ai.translate(anyMap(), any())).thenReturn(Map.of("found_place", "Student Union"));
        assertThatThrownBy(() -> service.translate(resource, request(), null))
            .isInstanceOfSatisfying(CustomException.class, error -> assertThat(error.getErrorCode()).isEqualTo(ApiResponseCode.SOURCE_CHANGED));
        verify(store, never()).succeed(anyString(), anyString(), anyString());
        verify(store).fail(ID, OWNER, TranslationReason.TEMPORARY_ERROR, null);
        verify(capacity).release();
    }

    @Test
    void lostOwnershipReturnsCurrentPendingInsteadOfLocallyGeneratedText() {
        prepareOwner();
        when(ai.translate(anyMap(), any())).thenReturn(Map.of("found_place", "late translation"));
        when(store.succeed(anyString(), anyString(), anyString())).thenReturn(false);
        when(store.findById(ID)).thenReturn(Optional.of(stored(TranslationStatus.PENDING, null)));
        var response = service.translate(resource, request(), null);
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.data()).isNull();
    }

    @Test
    void missingTranslatedFieldBecomesRetryableFailure() {
        prepareOwner();
        when(ai.translate(anyMap(), any())).thenReturn(Map.of("content", "wrong field"));
        when(store.findById(ID)).thenReturn(Optional.of(failed(TranslationReason.INVALID_TRANSLATION)));
        var response = service.translate(resource, request(), null);
        assertThat(response.status()).isEqualTo("FAILED");
        assertThat(response.reasonCode()).isEqualTo(TranslationReason.INVALID_TRANSLATION);
        assertThat(response.retryable()).isTrue();
        verify(store).fail(ID, OWNER, TranslationReason.INVALID_TRANSLATION, null);
        verify(store, never()).succeed(anyString(), anyString(), anyString());
        verify(capacity).release();
    }

    @Test
    void solarFailureStoresSafeReasonAndCooldown() {
        prepareOwner();
        when(ai.translate(anyMap(), any())).thenThrow(new TranslationFailureException(TranslationReason.RATE_LIMITED, Duration.ofSeconds(120)));
        when(store.findById(ID)).thenReturn(Optional.of(failed(TranslationReason.RATE_LIMITED)));
        assertThat(service.translate(resource, request(), null).reasonCode()).isEqualTo(TranslationReason.RATE_LIMITED);
        verify(store).fail(ID, OWNER, TranslationReason.RATE_LIMITED, Duration.ofSeconds(120));
        verify(capacity).release();
    }

    @Test
    void capacityRejectionNeverCallsSolarOrReleasesUnacquiredPermit() {
        when(reader.load(resource, null)).thenReturn(source);
        when(store.claim(any(), anyString(), any(), anyString(), anyString())).thenReturn(
            new TranslationStore.Claim(stored(TranslationStatus.PENDING, null), OWNER));
        when(store.findById(ID)).thenReturn(Optional.of(failed(TranslationReason.RATE_LIMITED)));
        assertThat(service.translate(resource, request(), null).reasonCode()).isEqualTo(TranslationReason.RATE_LIMITED);
        verify(store).fail(ID, OWNER, TranslationReason.RATE_LIMITED, Duration.ofMinutes(1));
        verifyNoInteractions(ai);
        verify(capacity, never()).release();
    }

    @Test
    void cachedPrivateResultRechecksPermissions() {
        var message = TranslationResource.message(TranslationType.RECRUITMENT_MESSAGE, 1, 2, "3");
        var record = new StoredTranslation(ID, message, source.version(), "en-US", TranslationStatus.SUCCESS,
            "{\"content\":\"secret\"}", null, null, null, LocalDateTime.now(clock).plusDays(1));
        when(store.findById(ID)).thenReturn(Optional.of(record));
        when(reader.load(message, 7)).thenThrow(AuthorizationException.withDetail("no longer a member"));
        assertThatThrownBy(() -> service.get(ID, 7)).isInstanceOf(AuthorizationException.class);
        verifyNoInteractions(ai, capacity);
    }

    @Test
    void expiredResultIsUnavailableEvenBeforePhysicalCleanup() {
        var current = stored(TranslationStatus.SUCCESS, "{}");
        when(store.findById(ID)).thenReturn(Optional.of(new StoredTranslation(ID, resource, source.version(), "en-US",
            current.status(), current.translatedJson(), null, null, null, LocalDateTime.now(clock))));
        assertThatThrownBy(() -> service.get(ID, null)).isInstanceOfSatisfying(CustomException.class,
            error -> assertThat(error.getErrorCode()).isEqualTo(ApiResponseCode.TRANSLATION_NOT_FOUND));
        verifyNoInteractions(reader, ai, capacity);
    }

    @Test
    void getDoesNotRetryExpiredLease() {
        when(reader.load(resource, null)).thenReturn(source);
        when(store.findById(ID)).thenReturn(Optional.of(new StoredTranslation(ID, resource, source.version(), "en-US",
            TranslationStatus.PENDING, null, null, LocalDateTime.now(clock), null, LocalDateTime.now(clock).plusDays(1))));
        var response = service.get(ID, null);
        assertThat(response.status()).isEqualTo("FAILED");
        assertThat(response.reasonCode()).isEqualTo(TranslationReason.TEMPORARY_ERROR);
        verify(store, never()).claim(any(), any(), any(), any(), any());
        verify(store, never()).fail(any(), any(), any(), any());
        verifyNoInteractions(ai, capacity);
    }

    @Test
    void existingResultWithChangedSourceIsRejectedOnGet() {
        when(reader.load(resource, null)).thenReturn(TranslationSource.of(resource,
            TranslationSourceFields.lostItem("새 원문", null), Map.of("found_place", "새 원문"), null));
        when(store.findById(ID)).thenReturn(Optional.of(stored(TranslationStatus.SUCCESS, "{}")));
        assertThatThrownBy(() -> service.get(ID, null)).isInstanceOfSatisfying(CustomException.class,
            error -> assertThat(error.getErrorCode()).isEqualTo(ApiResponseCode.SOURCE_CHANGED));
        verifyNoInteractions(ai);
    }

    @Test
    void unsupportedOrOversizedContentDoesNotCreateJob() {
        properties.setMaxInputCharacters(2);
        when(reader.load(resource, null)).thenReturn(source);
        assertThat(service.translate(resource, request(), null).reasonCode()).isEqualTo(TranslationReason.INPUT_TOO_LONG);
        when(reader.load(resource, null)).thenReturn(TranslationSource.of(resource, source.data(), Map.of(), TranslationReason.UNSUPPORTED_CONTENT));
        assertThat(service.translate(resource, request(), null).reasonCode()).isEqualTo(TranslationReason.UNSUPPORTED_CONTENT);
        verifyNoInteractions(store, ai, capacity);
    }

    @Test
    void koreanSummaryReturnsOriginalWithoutAi() {
        var summaryResource = TranslationResource.article(TranslationType.ARTICLE_SUMMARY, 1);
        var summary = TranslationSource.of(summaryResource, TranslationSourceFields.summary(List.of(Map.of("icon", "📌", "text", "신청 조건"))),
            Map.of("item_0", "신청 조건"), null);
        when(reader.load(summaryResource, null)).thenReturn(summary);
        var response = service.translate(summaryResource, new TranslationRequest("ko-kr", summary.version()), null);
        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.translationId()).isNull();
        assertThat(response.data()).isEqualTo(summary.data());
        verifyNoInteractions(store, ai, capacity);
    }

    private void prepareOwner() {
        when(reader.load(resource, null)).thenReturn(source);
        when(store.claim(any(), anyString(), any(), anyString(), anyString())).thenReturn(
            new TranslationStore.Claim(stored(TranslationStatus.PENDING, null), OWNER));
        when(capacity.tryAcquire()).thenReturn(true);
    }

    private TranslationRequest request() {
        return new TranslationRequest("en-us", source.version());
    }

    private StoredTranslation stored(TranslationStatus status, String data) {
        return new StoredTranslation(ID, resource, source.version(), "en-US", status, data, null,
            LocalDateTime.now(clock).plusMinutes(1), null, LocalDateTime.now(clock).plusDays(30));
    }

    private StoredTranslation failed(TranslationReason reason) {
        return new StoredTranslation(ID, resource, source.version(), "en-US", TranslationStatus.FAILED, null, reason,
            null, LocalDateTime.now(clock).plusSeconds(30), LocalDateTime.now(clock).plusDays(30));
    }
}
