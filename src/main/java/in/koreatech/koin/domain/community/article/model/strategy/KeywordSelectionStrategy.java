package in.koreatech.koin.domain.community.article.model.strategy;

import java.util.List;

public interface KeywordSelectionStrategy {
    List<String> getTopKeywords(int count);
}
