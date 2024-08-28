package in.koreatech.koin.domain.community.article.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.article.model.ArticleViewLog;

public interface ArticleViewLogRepository extends Repository<ArticleViewLog, Integer> {

    Optional<ArticleViewLog> findByArticleIdAndUserId(Integer articleId, Integer userId);

    ArticleViewLog save(ArticleViewLog articleViewLog);
}
