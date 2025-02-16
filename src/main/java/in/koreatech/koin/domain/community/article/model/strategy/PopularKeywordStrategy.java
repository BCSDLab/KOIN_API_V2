package in.koreatech.koin.domain.community.article.model.strategy;

import in.koreatech.koin.domain.community.article.model.redis.PopularKeywordTracker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PopularKeywordStrategy implements KeywordSelectionStrategy {

    private final PopularKeywordTracker popularKeywordTracker;

    @Override
    public List<String> getTopKeywords(int count) {
        Set<String> redisKeywords = popularKeywordTracker.getTopKeywords(count);
        return new ArrayList<>(redisKeywords);
    }
}
