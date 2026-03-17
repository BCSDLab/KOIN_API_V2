package in.koreatech.koin.common.event;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ArticleKeywordEvent(
    Integer articleId,
    Integer authorId,
    Map<Integer, String> matchedKeywordByUserId
) {

    public ArticleKeywordEvent {
        matchedKeywordByUserId = Collections.unmodifiableMap(new LinkedHashMap<>(matchedKeywordByUserId));
    }
}
