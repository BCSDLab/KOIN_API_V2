package in.koreatech.koin.domain.community.article.model.strategy;

import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BackupKeywordStrategy implements KeywordSelectionStrategy {

    private final ArticleSearchKeywordRepository keywordRepository;

    @Override
    public List<String> getTopKeywords(int count) {
        LocalDateTime fromDate = LocalDateTime.now().minusWeeks(1);
        Pageable pageable = PageRequest.of(0, count);

        List<String> topKeywords = keywordRepository.findTopKeywords(fromDate, pageable);

        if (topKeywords.isEmpty()) {
            topKeywords = keywordRepository.findTopKeywordsByLatest(pageable);
        }

        return topKeywords;
    }
}
