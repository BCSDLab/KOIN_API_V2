package in.koreatech.koin.domain.community.articles.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.community.articles.model.ArticleViewLog;

public interface ArticleViewLogRepository extends Repository<ArticleViewLog, Integer> {

    Optional<ArticleViewLog> findByArticleIdAndUserId(Integer articleId, Integer userId);

    ArticleViewLog save(ArticleViewLog articleViewLog);
}
