package in.koreatech.koin.admin.keyword.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.keyword.exception.ArticleKeywordNotFoundException;
import in.koreatech.koin.domain.community.keyword.enums.KeywordCategory;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;

public interface AdminArticleKeywordRepository extends Repository<ArticleKeyword, Integer> {

    Optional<ArticleKeyword> findByKeywordAndCategory(String keyword, KeywordCategory category);

    default ArticleKeyword getByKeyword(String keyword) {
        return findByKeywordAndCategory(keyword, KeywordCategory.KOREATECH)
            .orElseThrow(() -> ArticleKeywordNotFoundException.withDetail("keyword : " + keyword));
    }

    List<ArticleKeyword> findByIsFilteredAndCategory(boolean isFiltered, KeywordCategory category);
}
