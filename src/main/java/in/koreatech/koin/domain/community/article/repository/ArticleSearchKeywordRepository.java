package in.koreatech.koin.domain.community.article.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.article.model.ArticleSearchKeyword;

public interface ArticleSearchKeywordRepository extends Repository<ArticleSearchKeyword, Integer> {

    void save(ArticleSearchKeyword keyword);

    Optional<ArticleSearchKeyword> findByKeyword(String keywordStr);

    @Query("""
        SELECT k.keyword
        FROM ArticleSearchKeyword k
        WHERE k.lastSearchedAt >= :oneWeekAgo
        ORDER BY k.weight DESC, k.lastSearchedAt DESC
        """)
    List<String> findTopKeywords(LocalDateTime oneWeekAgo, Pageable pageable);

    @Query("""
        SELECT k.keyword
        FROM ArticleSearchKeyword k
        ORDER BY k.weight DESC, k.lastSearchedAt
        """)
    List<String> findTopKeywordsByLatest(Pageable pageable);

    List<ArticleSearchKeyword> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
}
