package in.koreatech.koin.domain.community.keywords.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.community.keywords.exception.ArticleKeywordUserMapNotFoundException;
import in.koreatech.koin.domain.community.keywords.model.ArticleKeywordUserMap;

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
    List<String> findAllKeywordbyUserId(@Param("userId") Integer userId);
}
