package in.koreatech.koin.domain.community.article.model.strategy;

import in.koreatech.koin.domain.community.article.model.redis.RedisKeywordTracker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class RedisKeywordStrategy implements KeywordRetrievalStrategy {

    private final RedisKeywordTracker redisKeywordTracker;

    @Override
    public List<String> getTopKeywords(int count) {
        Set<String> redisKeywords = redisKeywordTracker.getTopKeywords(count);
        return new ArrayList<>(redisKeywords);
    }
}
