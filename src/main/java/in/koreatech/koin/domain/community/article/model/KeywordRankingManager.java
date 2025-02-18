package in.koreatech.koin.domain.community.article.model;

import in.koreatech.koin.domain.community.article.model.redis.PopularKeywordTracker;
import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KeywordRankingManager {

    private final PopularKeywordTracker popularKeywordTracker;
    private final ArticleSearchKeywordRepository keywordRepository;

    public List<String> getTopKeywords(int count) {
        List<String> primaryKeywords = new ArrayList<>(popularKeywordTracker.getTopKeywords(count));

        if (primaryKeywords.size() < count) {
            int remainingCount = count - primaryKeywords.size();
            List<String> secondaryKeywords = getBackupKeywords(remainingCount);
            secondaryKeywords.stream()
                .filter(keyword -> !primaryKeywords.contains(keyword))
                .forEach(primaryKeywords::add);
        }

        return primaryKeywords;
    }

    private List<String> getBackupKeywords(int count) {
        LocalDateTime fromDate = LocalDateTime.now().minusWeeks(1);
        Pageable pageable = PageRequest.of(0, count);

        List<String> topKeywords = keywordRepository.findTopKeywords(fromDate, pageable);
        if (topKeywords.isEmpty()) {
            topKeywords = keywordRepository.findTopKeywordsByLatest(pageable);
        }
        return topKeywords;
    }
}
