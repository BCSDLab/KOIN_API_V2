package in.koreatech.koin.common.event;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import in.koreatech.koin.domain.community.keyword.enums.KeywordCategory;

public record ArticleKeywordEvent(
    Integer articleId,
    Integer authorId,
    KeywordCategory category,
    Map<Integer, String> matchedKeywordByUserId
) {

    public ArticleKeywordEvent {
        matchedKeywordByUserId = Collections.unmodifiableMap(new LinkedHashMap<>(matchedKeywordByUserId));
    }
}
