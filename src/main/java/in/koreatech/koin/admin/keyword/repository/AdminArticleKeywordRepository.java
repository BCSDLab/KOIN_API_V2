package in.koreatech.koin.admin.keyword.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.keyword.exception.ArticleKeywordNotFoundException;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.config.repository.JpaRepository;

@JpaRepository
public interface AdminArticleKeywordRepository extends Repository<ArticleKeyword, Integer> {

    Optional<ArticleKeyword> findByKeyword(String keyword);

    default ArticleKeyword getByKeyword(String keyword) {
        return findByKeyword(keyword)
            .orElseThrow(() -> ArticleKeywordNotFoundException.withDetail("keyword : " + keyword));
    }

    List<ArticleKeyword> findByIsFiltered(boolean isFiltered);
}
