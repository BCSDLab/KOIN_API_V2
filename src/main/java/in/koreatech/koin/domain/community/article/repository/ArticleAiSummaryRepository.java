package in.koreatech.koin.domain.community.article.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.community.article.model.ArticleAiSummary;

public interface ArticleAiSummaryRepository extends Repository<ArticleAiSummary, Integer> {

    ArticleAiSummary save(ArticleAiSummary articleAiSummary);

    Optional<ArticleAiSummary> findById(Integer id);

    Optional<ArticleAiSummary> findByArticleId(Integer articleId);

    @Modifying
    @Query(value = """
        INSERT INTO article_ai_summaries
            (article_id, status, source_fingerprint, source_updated_at, model, prompt_version, retry_count,
             is_deleted, created_at, updated_at)
        VALUES
            (:articleId, 'WAIT', :sourceFingerprint, :sourceUpdatedAt, :model, :promptVersion, 0,
             0, NOW(), NOW())
        ON DUPLICATE KEY UPDATE
            status = IF(is_deleted = 1, 'WAIT', status),
            source_fingerprint = IF(is_deleted = 1, :sourceFingerprint, source_fingerprint),
            source_updated_at = IF(is_deleted = 1, :sourceUpdatedAt, source_updated_at),
            model = IF(is_deleted = 1, :model, model),
            prompt_version = IF(is_deleted = 1, :promptVersion, prompt_version),
            retry_count = IF(is_deleted = 1, 0, retry_count),
            next_attempt_at = IF(is_deleted = 1, NULL, next_attempt_at),
            locked_until = IF(is_deleted = 1, NULL, locked_until),
            worker_id = IF(is_deleted = 1, NULL, worker_id),
            failure_reason = IF(is_deleted = 1, NULL, failure_reason),
            updated_at = IF(is_deleted = 1, NOW(), updated_at),
            is_deleted = 0
        """, nativeQuery = true)
    void insertWaitIfAbsent(
        @Param("articleId") Integer articleId,
        @Param("sourceFingerprint") String sourceFingerprint,
        @Param("sourceUpdatedAt") LocalDateTime sourceUpdatedAt,
        @Param("model") String model,
        @Param("promptVersion") String promptVersion
    );

    @Query("""
        SELECT DISTINCT s
        FROM ArticleAiSummary s
        JOIN FETCH s.article a
        LEFT JOIN FETCH a.attachments
        WHERE s.id = :id
        """)
    Optional<ArticleAiSummary> findByIdWithArticle(@Param("id") Integer id);

    @Query(value = """
        SELECT *
        FROM article_ai_summaries
        WHERE is_deleted = 0
          AND (
              (
                  status = 'WAIT'
                  AND (locked_until IS NULL OR locked_until < :now)
                  AND (next_attempt_at IS NULL OR next_attempt_at <= :now)
              )
              OR (
                  status = 'PROCESSING'
                  AND locked_until < :now
              )
          )
        ORDER BY
          CASE WHEN status = 'WAIT' THEN 0 ELSE 1 END,
          updated_at ASC
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<ArticleAiSummary> findWaitingSummariesForUpdate(
        @Param("now") LocalDateTime now,
        @Param("limit") int limit
    );

    @Query(value = """
        SELECT *
        FROM article_ai_summaries
        WHERE is_deleted = 0
          AND status = 'FAILED'
          AND retry_count < :maxRetryCount
          AND (next_attempt_at IS NULL OR next_attempt_at <= :now)
          AND (locked_until IS NULL OR locked_until < :now)
        ORDER BY
          next_attempt_at ASC,
          updated_at ASC
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<ArticleAiSummary> findRetryableFailedSummariesForUpdate(
        @Param("now") LocalDateTime now,
        @Param("limit") int limit,
        @Param("maxRetryCount") int maxRetryCount
    );

}
