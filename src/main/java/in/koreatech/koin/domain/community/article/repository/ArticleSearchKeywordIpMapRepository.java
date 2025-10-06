package in.koreatech.koin.domain.community.article.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.article.model.ArticleSearchKeyword;
import in.koreatech.koin.domain.community.article.model.ArticleSearchKeywordIpMap;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface ArticleSearchKeywordIpMapRepository extends Repository<ArticleSearchKeywordIpMap, Integer> {

    void save(ArticleSearchKeywordIpMap map);

    Optional<ArticleSearchKeywordIpMap> findByArticleSearchKeywordAndIpAddress(ArticleSearchKeyword keyword, String ipAddress);

    List<ArticleSearchKeywordIpMap> findByUpdatedAtBetween(LocalDateTime before, LocalDateTime now);
}
