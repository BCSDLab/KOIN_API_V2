package in.koreatech.koin.domain.translation.repository;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.translation.model.ContentTranslation;

public interface TranslationRepository extends Repository<ContentTranslation, String> {
    Optional<ContentTranslation> findById(String id);

    Optional<ContentTranslation> findByCacheKey(String cacheKey);

    @Modifying
    @Query(value = """
        INSERT INTO content_translations
            (id, cache_key, resource_type, resource_id, parent_id, room_id,
             source_version, target_language, model, prompt_version, status,
             next_attempt_at, expires_at, created_at, updated_at)
        VALUES (:id, :cacheKey, :resourceType, :resourceId, :parentId, :roomId,
                :sourceVersion, :targetLanguage, :model, :promptVersion, 'FAILED',
                :now, :expiresAt, :now, :now)
        ON DUPLICATE KEY UPDATE cache_key = cache_key
        """, nativeQuery = true)
    void insertIfAbsent(
        @Param("id") String id, @Param("cacheKey") String cacheKey,
        @Param("resourceType") String resourceType, @Param("resourceId") String resourceId,
        @Param("parentId") int parentId, @Param("roomId") int roomId,
        @Param("sourceVersion") String sourceVersion, @Param("targetLanguage") String targetLanguage,
        @Param("model") String model, @Param("promptVersion") String promptVersion,
        @Param("now") LocalDateTime now, @Param("expiresAt") LocalDateTime expiresAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE content_translations
        SET status = 'PENDING', owner_token = :ownerToken, locked_until = :lockedUntil,
            next_attempt_at = NULL, translated_json = NULL, reason_code = NULL,
            expires_at = :expiresAt, updated_at = :now
        WHERE cache_key = :cacheKey
          AND (expires_at <= :now
            OR (status = 'PENDING' AND (locked_until IS NULL OR locked_until <= :now))
            OR (status = 'FAILED' AND (next_attempt_at IS NULL OR next_attempt_at <= :now)))
        """, nativeQuery = true)
    int claim(@Param("cacheKey") String cacheKey, @Param("ownerToken") String ownerToken,
        @Param("now") LocalDateTime now, @Param("lockedUntil") LocalDateTime lockedUntil,
        @Param("expiresAt") LocalDateTime expiresAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE content_translations
        SET status = 'SUCCESS', translated_json = :translatedJson, reason_code = NULL,
            owner_token = NULL, locked_until = NULL, next_attempt_at = NULL, updated_at = :now
        WHERE id = :id AND status = 'PENDING' AND owner_token = :ownerToken AND locked_until > :now
        """, nativeQuery = true)
    int succeed(@Param("id") String id, @Param("ownerToken") String ownerToken,
        @Param("translatedJson") String translatedJson, @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE content_translations
        SET status = 'FAILED', translated_json = NULL, reason_code = :reason,
            owner_token = NULL, locked_until = NULL, next_attempt_at = :nextAttemptAt,
            expires_at = GREATEST(expires_at, :nextAttemptAt), updated_at = :now
        WHERE id = :id AND status = 'PENDING' AND owner_token = :ownerToken AND locked_until > :now
        """, nativeQuery = true)
    int fail(@Param("id") String id, @Param("ownerToken") String ownerToken,
        @Param("reason") String reason, @Param("nextAttemptAt") LocalDateTime nextAttemptAt,
        @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE content_translations
        SET status = 'FAILED', translated_json = NULL, reason_code = 'INVALID_TRANSLATION',
            owner_token = NULL, locked_until = NULL, next_attempt_at = :now, updated_at = :now
        WHERE id = :id AND status = 'SUCCESS'
          AND BINARY translated_json = BINARY :expectedJson
        """, nativeQuery = true)
    int invalidateSuccess(@Param("id") String id, @Param("expectedJson") String expectedJson,
        @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional(propagation = REQUIRES_NEW)
    @Query(value = """
        DELETE FROM content_translations
        WHERE expires_at <= :now
        ORDER BY expires_at
        LIMIT 1000
        """, nativeQuery = true)
    int deleteExpired(@Param("now") LocalDateTime now);
}
