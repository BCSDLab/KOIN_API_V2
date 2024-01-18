package in.koreatech.koin.domain.community.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.exception.ArticleNotFoundException;
import in.koreatech.koin.domain.community.model.Article;

public interface ArticleRepository extends Repository<Article, Long> {

    Page<Article> findByBoardId(Long boardId, Pageable pageable);

    Article save(Article article);

    Optional<Article> findById(Long articleId);

    default Article getById(Long articleId) {
        return findById(articleId).orElseThrow(() -> ArticleNotFoundException.withDetail("articleId: " + articleId));
    }
}
