package in.koreatech.koin.domain.community.article.model.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KeywordRetrievalContext {

    private final PopularKeywordStrategy popularKeywordStrategy;
    private final BackupKeywordStrategy backupKeywordStrategy;

    public List<String> retrieveTopKeywords(int count) {
        List<String> primaryKeywords = popularKeywordStrategy.getTopKeywords(count);
        List<String> finalKeywords = new ArrayList<>(primaryKeywords);

        if (primaryKeywords.size() < count) {
            int remainingCount = count - primaryKeywords.size();
            List<String> secondaryKeywords = backupKeywordStrategy.getTopKeywords(remainingCount);
            secondaryKeywords.stream()
                .filter(keyword -> !finalKeywords.contains(keyword))
                .forEach(finalKeywords::add);
        }

        return finalKeywords;
    }
}
