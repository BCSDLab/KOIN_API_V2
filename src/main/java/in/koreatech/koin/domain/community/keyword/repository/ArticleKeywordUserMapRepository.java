package in.koreatech.koin.domain.community.keyword.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.community.keyword.exception.ArticleKeywordUserMapNotFoundException;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import in.koreatech.koin.config.repository.JpaRepository;

@JpaRepository
public interface ArticleKeywordUserMapRepository extends Repository<ArticleKeywordUserMap, Integer> {

    ArticleKeywordUserMap save(ArticleKeywordUserMap articleKeywordUserMap);

    Long countByUserId(Integer userId);

    void deleteById(Integer keywordId);

    boolean existsByArticleKeywordId(Integer id);

    Optional<ArticleKeywordUserMap> findById(Integer keywordUserMapId);

    default ArticleKeywordUserMap getById(Integer keywordUserMapId) {
        return findById(keywordUserMapId).orElseThrow(
            () -> ArticleKeywordUserMapNotFoundException.withDetail("keywordUserMapId: " + keywordUserMapId));
    }

    List<ArticleKeywordUserMap> findAllByUserId(Integer userId);

    @Query("""
        SELECT akw.keyword FROM ArticleKeywordUserMap akum
        JOIN akum.articleKeyword akw
        WHERE akum.user.id = :userId
        """)
    List<String> findAllKeywordByUserId(@Param("userId") Integer userId);

    @Query(value = """
    SELECT * FROM article_keyword_user_map akum
    WHERE akum.keyword_id = :articleKeywordId
      AND akum.user_id = :userId
    """, nativeQuery = true)
    Optional<ArticleKeywordUserMap> findByArticleKeywordIdAndUserIdIncludingDeleted(
        @Param("articleKeywordId") Integer articleKeywordId,
        @Param("userId") Integer userId
    );
}
