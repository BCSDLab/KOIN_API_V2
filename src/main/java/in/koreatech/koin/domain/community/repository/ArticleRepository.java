package in.koreatech.koin.domain.community.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.exception.ArticleNotFoundException;
import in.koreatech.koin.domain.community.model.Article;

public interface ArticleRepository extends Repository<Article, Long> {

    Article save(Article article);

    Page<Article> findByBoardId(Long boardId, Pageable pageable);

    default Page<Article> getByBoardId(Long boardId, Pageable pageable) {
        return findByBoardId(boardId, pageable);
    }

    Optional<Article> findById(Long articleId);

    default Article getById(Long articleId) {
        return findById(articleId).orElseThrow(
            () -> ArticleNotFoundException.withDetail(
                "articleId: " + articleId));
    }
}
