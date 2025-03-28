package in.koreatech.koin.domain.community.article.model.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PopularKeywordTracker {

    private static final double FIXED_WEIGHT_AFTER_FIVE_SEARCHES = 0.0625;
    private static final int MAX_SEARCH_COUNT_FOR_WEIGHT = 10;

    private static final String KEYWORD_SET = "popular_keywords";
    private static final String IP_SEARCH_COUNT_PREFIX = "search:count:ip:";

    private final RedisTemplate<String, Object> redisTemplate;

    public void updateKeywordWeight(String ipAddress, String keyword) {
        if (keyword == null || keyword.isBlank() || ipAddress == null || ipAddress.isBlank()) {
            return;
        }

        String ipSearchCountKey = IP_SEARCH_COUNT_PREFIX + ipAddress;

        Long currentIpCount = redisTemplate.opsForHash().increment(ipSearchCountKey, keyword, 1);

        double additionalWeight = calculateWeight(currentIpCount);

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

    public Set<String> getTopKeywords(int limit) {
        Set<TypedTuple<Object>> keywordTuples = redisTemplate.opsForZSet()
            .reverseRangeWithScores(KEYWORD_SET, 0, limit - 1);

        if (keywordTuples == null || keywordTuples.isEmpty()) {
            return Set.of();
        }

        return keywordTuples.stream()
            .map(TypedTuple::getValue)
            .map(String::valueOf)
            .collect(Collectors.toSet());
    }
}
