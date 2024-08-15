package in.koreatech.koin.domain.community.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.exception.ArticleKeywordUserMapNotFoundException;
import in.koreatech.koin.domain.community.model.ArticleKeywordUserMap;

public interface ArticleKeywordUserMapRepository extends Repository<ArticleKeywordUserMap, Integer> {

    ArticleKeywordUserMap save(ArticleKeywordUserMap articleKeywordUserMap);

    Integer countByUserId(Integer userId);

    void deleteById(Integer keywordId);

    boolean existsByArticleKeywordId(Integer id);

    Optional<ArticleKeywordUserMap> findById(Integer keywordUserMapId);

    default ArticleKeywordUserMap getById(Integer keywordUserMapId) {
        return findById(keywordUserMapId).orElseThrow(
            () -> ArticleKeywordUserMapNotFoundException.withDetail("keywordUserMapId: " + keywordUserMapId));
    }
}
