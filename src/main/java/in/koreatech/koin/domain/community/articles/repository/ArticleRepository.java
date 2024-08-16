package in.koreatech.koin.domain.community.articles.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.articles.exception.ArticleNotFoundException;
import in.koreatech.koin.domain.community.articles.model.Article;

public interface ArticleRepository extends Repository<Article, Integer> {

    Article save(Article article);

    Page<Article> findByIsNotice(Boolean isNotice, Pageable pageable);

    Optional<Article> findById(Integer articleId);

    List<Article> findAll(Pageable pageable);

    Page<Article> findByBoardId(Integer boardId, PageRequest pageRequest);

    default Article getById(Integer articleId) {
        return findById(articleId).orElseThrow(
            () -> ArticleNotFoundException.withDetail(
                "articleId: " + articleId));
    }
}
