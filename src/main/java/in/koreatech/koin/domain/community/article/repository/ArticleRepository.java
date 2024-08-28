package in.koreatech.koin.domain.community.article.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.article.exception.ArticleNotFoundException;
import in.koreatech.koin.domain.community.article.model.Article;

public interface ArticleRepository extends Repository<Article, Integer> {

    Article save(Article article);

    Page<Article> findAllByIsNotice(Boolean isNotice, Pageable pageable);

    Optional<Article> findById(Integer articleId);

    List<Article> findAll(Pageable pageable);

    Page<Article> findAllByBoardId(Integer boardId, PageRequest pageRequest);

    default Article getById(Integer articleId) {
        return findById(articleId).orElseThrow(
            () -> ArticleNotFoundException.withDetail(
                "articleId: " + articleId));
    }

    Page<Article> findAllByBoardIdAndTitleContaining(Integer boardId, String query, PageRequest pageRequest);

    Page<Article> findAllByTitleContaining(String query, PageRequest pageRequest);

    Page<Article> findAllByIsNoticeAndTitleContaining(Boolean isNotice, String query, PageRequest pageRequest);

    Long countBy();
}
