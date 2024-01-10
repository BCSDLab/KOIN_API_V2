package in.koreatech.koin.domain.community.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.model.Article;

public interface ArticleRepository extends Repository<Article, Long> {

    Optional<Article> findByBoardId(Long boardId);
}
