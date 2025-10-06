package in.koreatech.koin.domain.community.article.repository.redis;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.article.model.redis.ArticleHitUser;
import in.koreatech.koin.config.repository.RedisRepositoryMarker;

@RedisRepositoryMarker
public interface ArticleHitUserRepository extends Repository<ArticleHitUser, String> {

    ArticleHitUser save(ArticleHitUser articleHitUser);

    Optional<ArticleHitUser> findById(String id);

    default Optional<ArticleHitUser> findByArticleIdAndPublicIp(Integer articleId, String publicIp) {
        return findById(articleId + ArticleHitUser.DELIMITER + publicIp);
    }
}
