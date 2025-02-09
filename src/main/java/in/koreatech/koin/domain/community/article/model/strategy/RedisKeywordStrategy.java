package in.koreatech.koin.domain.community.article.model.strategy;

import in.koreatech.koin.domain.community.article.model.redis.RedisKeywordTracker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisKeywordStrategy implements KeywordRetrievalStrategy {

    private final RedisKeywordTracker redisKeywordTracker;

    @Override
    public List<String> getTopKeywords(int count) {
        Set<Object> redisKeywords = redisKeywordTracker.getTopKeywords(count);
        return redisKeywords.stream().map(Object::toString).collect(Collectors.toList());
    }
}
