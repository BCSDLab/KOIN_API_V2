package in.koreatech.koin.domain.community.keyword.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.community.article.dto.ArticleKeywordResult;
import in.koreatech.koin.domain.community.keyword.enums.KeywordCategory;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;

public interface ArticleKeywordRepository extends Repository<ArticleKeyword, Integer> {

    Optional<ArticleKeyword> findByKeywordAndCategory(String keyword, KeywordCategory category);

    @Query(value = """
    SELECT * FROM article_keywords ak
    WHERE ak.keyword = :keyword
      AND ak.category = :category
    """, nativeQuery = true)
    Optional<ArticleKeyword> findByKeywordAndCategoryIncludingDeleted(
        @Param("keyword") String keyword,
        @Param("category") String category
    );

    ArticleKeyword save(ArticleKeyword articleKeyword);

    Optional<ArticleKeyword> findById(Integer id);

    List<ArticleKeyword> findAllByCategory(KeywordCategory category, Pageable pageable);

    @Query("""
    SELECT new in.koreatech.koin.domain.community.article.dto.ArticleKeywordResult(k.id, k.keyword, COUNT(u))
    FROM ArticleKeywordUserMap u
    JOIN u.articleKeyword k
    WHERE k.lastUsedAt >= :oneWeekAgo
      AND k.isFiltered = false
      AND k.category = :category
    GROUP BY k.id, k.keyword
    ORDER BY COUNT(u) DESC
    """)
    List<ArticleKeywordResult> findTopKeywordsInLastWeekExcludingFiltered(
        LocalDateTime oneWeekAgo,
        KeywordCategory category,
        Pageable top15
    );

    @Query("""
    SELECT new in.koreatech.koin.domain.community.article.dto.ArticleKeywordResult(k.id, k.keyword, COUNT(u))
    FROM ArticleKeyword k
    LEFT JOIN k.articleKeywordUserMaps u
    WHERE k.isFiltered = false
      AND k.category = :category
    GROUP BY k.id, k.keyword
    ORDER BY k.createdAt DESC
    """)
    List<ArticleKeywordResult> findTop15KeywordsExcludingFiltered(KeywordCategory category, Pageable top15);
}
