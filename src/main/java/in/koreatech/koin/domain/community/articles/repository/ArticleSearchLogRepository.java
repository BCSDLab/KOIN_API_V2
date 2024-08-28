package in.koreatech.koin.domain.community.articles.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.articles.model.ArticleSearchLog;

public interface ArticleSearchLogRepository extends Repository<ArticleSearchLog, Integer> {

    void save(ArticleSearchLog newLog);

    Optional<ArticleSearchLog> findByKeywordAndIpAddress(String query, String ipAddress);

    @Query("""
        SELECT log.keyword
        FROM ArticleSearchLog log
        WHERE log.updatedAt >= :oneDayAgo
        GROUP BY log.keyword
        ORDER BY COUNT(log.keyword) DESC
        """)
    List<String> findTopKeywords(LocalDateTime oneDayAgo, Pageable pageable);

    @Query("""
        SELECT log.keyword
        FROM ArticleSearchLog log
        GROUP BY log.keyword
        ORDER BY MAX(log.updatedAt) DESC, COUNT(log.keyword) DESC
        """)
    List<String> findTopKeywordsByLatest(Pageable pageable);
}
