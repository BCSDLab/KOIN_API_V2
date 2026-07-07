package in.koreatech.koin.domain.community.article.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.admin.article.dto.AdminArticleAiSummaryLogProjection;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummaryLog;

public interface ArticleAiSummaryLogRepository extends Repository<ArticleAiSummaryLog, Integer> {

    ArticleAiSummaryLog save(ArticleAiSummaryLog log);

    @Modifying
    @Query("""
        DELETE FROM ArticleAiSummaryLog l
        WHERE l.createdAt < :threshold
        """)
    int deleteOlderThan(@Param("threshold") LocalDateTime threshold);

    @Query(value = """
        SELECT
          l.id AS logId,
          l.summary_id AS summaryId,
          l.article_id AS articleId,
          l.board_id AS boardId,
          a.title AS articleTitle,
          l.event_type AS eventType,
          l.status AS status,
          l.failure_type AS failureType,
          l.message AS message,
          l.retry_count AS retryCount,
          l.next_attempt_at AS nextAttemptAt,
          l.worker_id AS workerId,
          l.created_at AS createdAt
        FROM article_ai_summary_logs l
        LEFT JOIN new_articles a ON a.id = l.article_id AND a.is_deleted = 0
        WHERE (:summaryId IS NULL OR l.summary_id = :summaryId)
          AND (:articleId IS NULL OR l.article_id = :articleId)
          AND (:eventType IS NULL OR l.event_type = :eventType)
          AND (:failureType IS NULL OR l.failure_type = :failureType)
        ORDER BY l.created_at DESC, l.id DESC
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM article_ai_summary_logs l
        WHERE (:summaryId IS NULL OR l.summary_id = :summaryId)
          AND (:articleId IS NULL OR l.article_id = :articleId)
          AND (:eventType IS NULL OR l.event_type = :eventType)
          AND (:failureType IS NULL OR l.failure_type = :failureType)
        """,
        nativeQuery = true)
    Page<AdminArticleAiSummaryLogProjection> findAdminLogs(
        @Param("summaryId") Integer summaryId,
        @Param("articleId") Integer articleId,
        @Param("eventType") String eventType,
        @Param("failureType") String failureType,
        Pageable pageable
    );
}
