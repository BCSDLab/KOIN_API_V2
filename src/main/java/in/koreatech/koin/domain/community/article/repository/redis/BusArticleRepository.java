package in.koreatech.koin.domain.community.article.repository.redis;

import in.koreatech.koin.domain.community.article.model.redis.BusNoticeArticle;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class BusArticleRepository {

    public static final String BUS_NOTICE_KEY = "busNoticeArticle";
    private final RedisTemplate<String, Object> redisTemplate;

    public void save(BusNoticeArticle article) {
        Map<Object, Object> existingArticle = redisTemplate.opsForHash().entries(BUS_NOTICE_KEY);

        if (!existingArticle.isEmpty()) {
            Object existingId = existingArticle.get("id");
            if (existingId.equals(article.getId())) {
                return;
            }
        }

        redisTemplate.delete(BUS_NOTICE_KEY);
        redisTemplate.opsForHash().put(BUS_NOTICE_KEY, "id", article.getId());
        redisTemplate.opsForHash().put(BUS_NOTICE_KEY, "title", article.getTitle());
    }
}
