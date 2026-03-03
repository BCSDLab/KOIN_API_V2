package in.koreatech.koin.common.event;

import java.util.Map;

public record ArticleKeywordEvent(
    Integer articleId,
    Integer authorId,
    Map<Integer, String> matchedKeywordByUserId
) {

}
