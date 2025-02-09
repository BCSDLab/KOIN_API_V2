package in.koreatech.koin.domain.community.article.model.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisKeywordTracker {

    private static final double FIXED_WEIGHT_AFTER_FIVE_SEARCHES = 0.0625;
    private static final int MAX_SEARCH_COUNT_FOR_WEIGHT = 10;
    private static final int DUPLICATE_CHECK_EXPIRATION_SECONDS = 3600;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEYWORD_SET = "popular_keywords";
    private static final String SEARCH_COUNT_PREFIX = "search:count:";
    private static final String DUPLICATE_CHECK_PREFIX = "search:duplicate:";

    public void updateKeywordWeight(String keyword, String ipAddress) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }

        String duplicateKey = DUPLICATE_CHECK_PREFIX + keyword + ":" + ipAddress;
        String searchCountKey = SEARCH_COUNT_PREFIX + keyword;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(duplicateKey))) {
            return;
        }

        redisTemplate.opsForValue().set(duplicateKey, "1", DUPLICATE_CHECK_EXPIRATION_SECONDS, TimeUnit.SECONDS);

        Long currentCount = redisTemplate.opsForValue().increment(searchCountKey, 1);
        if (currentCount == null) currentCount = 1L;

        double additionalWeight = calculateWeight(currentCount);
        if (additionalWeight > 0) {
            redisTemplate.opsForZSet().incrementScore(KEYWORD_SET, keyword, additionalWeight);
        }
    }

    private double calculateWeight(Long currentCount) {
        if (currentCount <= 5) {
            return 1.0 / Math.pow(2, currentCount - 1);
        } else if (currentCount <= MAX_SEARCH_COUNT_FOR_WEIGHT) {
            return FIXED_WEIGHT_AFTER_FIVE_SEARCHES;
        }
        return 0.0;
    }

    public Set<Object> getTopKeywords(int limit) {
        Set<TypedTuple<Object>> keywordTuples = redisTemplate.opsForZSet().reverseRangeWithScores(KEYWORD_SET, 0, limit - 1);
        return keywordTuples.stream().map(TypedTuple::getValue).collect(Collectors.toSet());
    }

    public Double getCurrentWeight(String keyword) {
        return redisTemplate.opsForZSet().score(KEYWORD_SET, keyword);
    }
}
