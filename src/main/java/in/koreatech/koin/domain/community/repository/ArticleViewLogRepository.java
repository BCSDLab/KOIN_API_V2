package in.koreatech.koin.domain.community.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.model.ArticleViewLog;

public interface ArticleViewLogRepository extends Repository<ArticleViewLog, Long> {

    Optional<ArticleViewLog> findByArticleIdAndUserId(Long articleId, Long userId);

    ArticleViewLog save(ArticleViewLog articleViewLog);
}
