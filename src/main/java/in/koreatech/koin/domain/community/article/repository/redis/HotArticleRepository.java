package in.koreatech.koin.domain.community.article.repository.redis;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class HotArticleRepository {

    public static final String REDIS_KEY = "hotArticles";

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveArticlesWithHitToRedis(Map<Integer, Integer> articlesWithHit, int limit) {
        List<Integer> sortedArticles = articlesWithHit.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(limit)
            .map(Map.Entry::getKey)
            .toList();
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        redisTemplate.delete(REDIS_KEY);
        for (Integer articleId : sortedArticles) {
            listOps.rightPush(REDIS_KEY, articleId);
        }
    }

    public List<Integer> getHotArticles(int limit) {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        List<Object> cache = listOps.range(REDIS_KEY, 0, -1);
        if (cache == null) {
            return List.of();
        }
        return cache.stream()
            .map(Integer.class::cast)
            .limit(limit)
            .toList();
    }
}
