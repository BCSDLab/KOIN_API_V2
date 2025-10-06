package in.koreatech.koin.domain.community.article.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.community.article.exception.ArticleNotFoundException;
import in.koreatech.koin.domain.community.article.model.KoinArticle;
import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface LostItemArticleRepository extends Repository<LostItemArticle, Integer>, LostItemArticleCustomRepository {

    KoinArticle save(LostItemArticle article);

    @Query(value = "SELECT * FROM lost_item_articles WHERE article_id = :articleId AND is_deleted = false", nativeQuery = true)
    Optional<LostItemArticle> findByArticleId(@Param("articleId") Integer articleId);

    default LostItemArticle getByArticleId(Integer articleId) {
        return findByArticleId(articleId).orElseThrow(
            () -> ArticleNotFoundException.withDetail("articleId: " + articleId));
    }
}
