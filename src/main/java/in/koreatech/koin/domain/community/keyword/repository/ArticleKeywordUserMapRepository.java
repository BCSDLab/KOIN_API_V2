package in.koreatech.koin.domain.community.keyword.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.community.keyword.enums.KeywordCategory;
import in.koreatech.koin.domain.community.keyword.exception.ArticleKeywordUserMapNotFoundException;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;

public interface ArticleKeywordUserMapRepository extends Repository<ArticleKeywordUserMap, Integer> {

    ArticleKeywordUserMap save(ArticleKeywordUserMap articleKeywordUserMap);

    Long countByUserIdAndArticleKeywordCategory(Integer userId, KeywordCategory category);

    boolean existsByArticleKeywordId(Integer id);

    Optional<ArticleKeywordUserMap> findById(Integer keywordUserMapId);

    default ArticleKeywordUserMap getById(Integer keywordUserMapId) {
        return findById(keywordUserMapId).orElseThrow(
            () -> ArticleKeywordUserMapNotFoundException.withDetail("keywordUserMapId: " + keywordUserMapId));
    }

    List<ArticleKeywordUserMap> findAllByUserIdAndArticleKeywordCategory(Integer userId, KeywordCategory category);

    @Query(value = """
    SELECT * FROM article_keyword_user_map akum
    WHERE akum.keyword_id = :articleKeywordId
      AND akum.user_id = :userId
    """, nativeQuery = true)
    Optional<ArticleKeywordUserMap> findByArticleKeywordIdAndUserIdIncludingDeleted(
        @Param("articleKeywordId") Integer articleKeywordId,
        @Param("userId") Integer userId
    );

    @Query("""
        SELECT akum
        FROM ArticleKeywordUserMap akum
        JOIN FETCH akum.articleKeyword akw
        JOIN FETCH akum.user
        WHERE akw.category = :category
          AND akw.keyword IN :keywords
          AND akum.isDeleted = false
        """)
    List<ArticleKeywordUserMap> findAllByArticleKeywordCategoryAndArticleKeywordKeywordIn(
        @Param("category") KeywordCategory category,
        @Param("keywords") List<String> keywords
    );
}
