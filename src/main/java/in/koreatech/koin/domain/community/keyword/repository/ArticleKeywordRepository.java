package in.koreatech.koin.domain.community.keyword.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.article.dto.ArticleKeywordResult;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;

public interface ArticleKeywordRepository extends Repository<ArticleKeyword, Integer> {

    Optional<ArticleKeyword> findByKeyword(String keyword);

    ArticleKeyword save(ArticleKeyword articleKeyword);

    void deleteById(Integer id);

    Optional<ArticleKeyword> findById(Integer id);

    @Query("""
        SELECT new in.koreatech.koin.domain.community.article.dto.ArticleKeywordResult(k.id, k.keyword, COUNT(u))
        FROM ArticleKeywordUserMap u
        JOIN u.articleKeyword k
        WHERE k.lastUsedAt >= :oneWeekAgo
        GROUP BY k.id, k.keyword
        ORDER BY COUNT(u) DESC
        """)
    List<ArticleKeywordResult> findTopKeywordsInLastWeek(LocalDateTime oneWeekAgo, Pageable pageable);

    @Query("""
        SELECT new in.koreatech.koin.domain.community.article.dto.ArticleKeywordResult(k.id, k.keyword, COUNT(u))
        FROM ArticleKeyword k
        LEFT JOIN k.articleKeywordUserMaps u
        GROUP BY k.id, k.keyword
        ORDER BY k.createdAt DESC
        """)
    List<ArticleKeywordResult> findTop15Keywords(Pageable pageable);

    List<ArticleKeyword> findAll(Pageable pageable);
}
