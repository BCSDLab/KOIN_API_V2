package in.koreatech.koin.domain.translation.model;

import java.time.LocalDateTime;

public record StoredTranslation(
    String id,
    TranslationResource resource,
    String sourceVersion,
    String targetLanguage,
    TranslationStatus status,
    String translatedJson,
    TranslationReason reason,
    LocalDateTime lockedUntil,
    LocalDateTime nextAttemptAt,
    LocalDateTime expiresAt
) {
}
