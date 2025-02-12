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

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEYWORD_SET = "popular_keywords";
    private static final String IP_SEARCH_COUNT_PREFIX = "search:count:ip:";

    public void updateKeywordWeight(String ipAddress, String keyword) {
        if (keyword == null || keyword.isBlank() || ipAddress == null || ipAddress.isBlank()) {
            return;
        }

        String ipSearchCountKey = IP_SEARCH_COUNT_PREFIX + ipAddress + ":" + keyword;

        // IP별 검색 횟수 증가
        Long currentIpCount = redisTemplate.opsForValue().increment(ipSearchCountKey, 1);
        if (currentIpCount == null) currentIpCount = 1L;

        // 가중치 계산
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

    public Set<Object> getTopKeywords(int limit) {
        Set<TypedTuple<Object>> keywordTuples = redisTemplate.opsForZSet().reverseRangeWithScores(KEYWORD_SET, 0, limit - 1);
        return keywordTuples.stream().map(TypedTuple::getValue).collect(Collectors.toSet());
    }
}
