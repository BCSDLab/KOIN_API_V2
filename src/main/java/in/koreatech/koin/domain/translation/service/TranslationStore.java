package in.koreatech.koin.domain.translation.service;

import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.translation.model.ContentTranslation;
import in.koreatech.koin.domain.translation.model.StoredTranslation;
import in.koreatech.koin.domain.translation.model.TranslationReason;
import in.koreatech.koin.domain.translation.model.TranslationResource;
import in.koreatech.koin.domain.translation.repository.TranslationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(propagation = REQUIRES_NEW)
public class TranslationStore {
    private final TranslationRepository repository;
    private final TranslationProperties properties;
    private final Clock clock;

    @Transactional(propagation = REQUIRES_NEW, readOnly = true)
    public Optional<StoredTranslation> findById(String id) {
        return repository.findById(id).map(ContentTranslation::snapshot);
    }

    public Claim claim(TranslationResource resource, String sourceVersion, SupportedLanguage language,
        String model, String promptVersion) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime expiresAt = now.plusDays(resource.type().isMessage()
            ? properties.getMessageRetentionDays() : properties.getContentRetentionDays());
        String cacheKey = TranslationSourceVersion.of(Map.of(
            "type", resource.type().name(), "id", resource.resourceId(),
            "parent_id", resource.parentId(), "room_id", resource.roomId(),
            "source_version", sourceVersion, "target_language", language.getCode(),
            "model", model, "prompt_version", promptVersion
        ));
        repository.insertIfAbsent(UUID.randomUUID().toString(), cacheKey, resource.type().name(),
            resource.resourceId(), resource.parentId(), resource.roomId(), sourceVersion, language.getCode(),
            model, promptVersion, now, expiresAt);
        String ownerToken = UUID.randomUUID().toString();
        int claimed = repository.claim(cacheKey, ownerToken, now,
            now.plusSeconds(properties.getRequestTimeoutSeconds() + 30L), expiresAt);
        StoredTranslation stored = repository.findByCacheKey(cacheKey).orElseThrow().snapshot();
        return new Claim(stored, claimed == 1 ? ownerToken : null);
    }

    public boolean succeed(String id, String ownerToken, String translatedJson) {
        return repository.succeed(id, ownerToken, translatedJson, LocalDateTime.now(clock)) == 1;
    }

    public boolean fail(String id, String ownerToken, TranslationReason reason, Duration retryAfter) {
        LocalDateTime now = LocalDateTime.now(clock);
        // 외부 Retry-After를 단축하지 않는다. 과도하게 큰 값만 MySQL DATETIME 범위로 제한한다.
        long maximumDelay = Duration.between(now, LocalDateTime.of(9999, 12, 31, 23, 59, 59)).getSeconds();
        long requestedDelay = retryAfter == null ? 0 : Math.min(maximumDelay, retryAfter.getSeconds());
        if (retryAfter != null && retryAfter.getNano() > 0 && requestedDelay < maximumDelay) {
            requestedDelay++;
        }
        long delay = Math.max(properties.getRetryDelaySeconds(), requestedDelay);
        return repository.fail(id, ownerToken, reason.name(), now.plusSeconds(delay), now) == 1;
    }

    public boolean invalidateSuccess(String id, String expectedJson) {
        return repository.invalidateSuccess(id, expectedJson, LocalDateTime.now(clock)) == 1;
    }

    @Scheduled(cron = "${translation.cleanup-cron:0 20 * * * *}")
    @Transactional(propagation = NOT_SUPPORTED)
    public int deleteExpired() {
        LocalDateTime cutoff = LocalDateTime.now(clock);
        int total = 0;
        int deleted;
        do {
            deleted = repository.deleteExpired(cutoff);
            total += deleted;
        } while (deleted == 1000);
        return total;
    }

    public record Claim(StoredTranslation translation, String ownerToken) {
    }
}
