package in.koreatech.koin.domain.community.keywords.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.keywords.model.ArticleKeyword;

public interface ArticleKeywordRepository extends Repository<ArticleKeyword, Integer> {

    Optional<ArticleKeyword> findByKeyword(String keyword);

    ArticleKeyword save(ArticleKeyword articleKeyword);

    void deleteById(Integer id);

    Optional<ArticleKeyword> findById(Integer id);

    @Query("""
        SELECT k.id, k.keyword, COUNT(u) AS keyword_count
        FROM ArticleKeywordUserMap u
        JOIN u.articleKeyword k
        WHERE k.lastUsedAt >= :oneWeekAgo
        GROUP BY k.id, k.keyword
        ORDER BY keyword_count DESC
        """)
    List<Object[]> findTopKeywordsInLastWeek(LocalDateTime oneWeekAgo, Pageable pageable);

    @Query("""
        SELECT k.id, k.keyword
        FROM ArticleKeyword k
        ORDER BY k.createdAt DESC
        """)
    List<Object[]> findTop15Keywords(Pageable pageable);
}
