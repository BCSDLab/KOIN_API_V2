package in.koreatech.koin.common.event;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record KoreatechArticleKeywordEvent(
    Integer articleId,
    Map<Integer, String> matchedKeywordByUserId
) {

    public KoreatechArticleKeywordEvent {
        matchedKeywordByUserId = Collections.unmodifiableMap(new LinkedHashMap<>(matchedKeywordByUserId));
    }
}
