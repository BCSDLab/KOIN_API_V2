package in.koreatech.koin.domain.translation.model;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "content_translations",
    uniqueConstraints = @UniqueConstraint(name = "uk_content_translations_cache_key", columnNames = "cache_key"),
    indexes = @Index(name = "idx_content_translations_expires_at", columnList = "expires_at"))
@NoArgsConstructor(access = PROTECTED)
public class ContentTranslation {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "cache_key", length = 64, nullable = false)
    private String cacheKey;

    @Enumerated(STRING)
    @Column(name = "resource_type", length = 32, nullable = false)
    private TranslationType resourceType;

    @Column(name = "resource_id", length = 32, nullable = false)
    private String resourceId;

    @Column(name = "parent_id", nullable = false)
    private int parentId;

    @Column(name = "room_id", nullable = false)
    private int roomId;

    @Column(name = "source_version", length = 64, nullable = false)
    private String sourceVersion;

    @Column(name = "target_language", length = 35, nullable = false)
    private String targetLanguage;

    @Column(length = 100, nullable = false)
    private String model;

    @Column(name = "prompt_version", length = 20, nullable = false)
    private String promptVersion;

    @Enumerated(STRING)
    @Column(length = 16, nullable = false)
    private TranslationStatus status;

    @Column(name = "translated_json", columnDefinition = "MEDIUMTEXT")
    private String translatedJson;

    @Enumerated(STRING)
    @Column(name = "reason_code", length = 40)
    private TranslationReason reason;

    @Column(name = "owner_token", length = 36)
    private String ownerToken;

    @Convert(disableConversion = true)
    @Column(name = "locked_until", columnDefinition = "DATETIME(6)")
    private LocalDateTime lockedUntil;

    @Convert(disableConversion = true)
    @Column(name = "next_attempt_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime nextAttemptAt;

    @Convert(disableConversion = true)
    @Column(name = "expires_at", columnDefinition = "DATETIME(6)", nullable = false)
    private LocalDateTime expiresAt;

    @Convert(disableConversion = true)
    @Column(name = "created_at", columnDefinition = "DATETIME(6)", nullable = false)
    private LocalDateTime createdAt;

    @Convert(disableConversion = true)
    @Column(name = "updated_at", columnDefinition = "DATETIME(6)", nullable = false)
    private LocalDateTime updatedAt;

    public StoredTranslation snapshot() {
        return new StoredTranslation(id,
            new TranslationResource(resourceType, resourceId, parentId, roomId),
            sourceVersion, targetLanguage, status, translatedJson, reason,
            lockedUntil, nextAttemptAt, expiresAt);
    }
}
