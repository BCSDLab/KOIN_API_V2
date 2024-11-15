package in.koreatech.koin.domain.community.article.model.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash(value = "articleHitUser")
public class ArticleHitUser {

    public static final String DELIMITER = ":";
    private static final long CACHE_EXPIRE_HOURS = 1L;

    @Id
    private String id;

    @TimeToLive(unit = TimeUnit.HOURS)
    private final Long expiration;

    @Builder
    private ArticleHitUser(String id, Long expiration) {
        this.id = id;
        this.expiration = expiration;
    }

    public static ArticleHitUser of(Integer articleId, String publicIp) {
        return ArticleHitUser.builder()
            .id(articleId + DELIMITER + publicIp)
            .expiration(CACHE_EXPIRE_HOURS)
            .build();
    }
}
