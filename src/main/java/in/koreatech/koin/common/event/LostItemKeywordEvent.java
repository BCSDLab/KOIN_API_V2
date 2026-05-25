package in.koreatech.koin.common.event;

import java.util.Map;

public record LostItemKeywordEvent(
    Integer articleId,
    String articleTitle,
    Integer authorId,
    MatchedKeywordUsers matchedKeywordUsers
) {

    public static LostItemKeywordEvent of(
        Integer articleId,
        String articleTitle,
        Integer authorId,
        Map<Integer, String> keywordByUserId
    ) {
        return new LostItemKeywordEvent(
            articleId,
            articleTitle,
            authorId,
            MatchedKeywordUsers.from(keywordByUserId)
        );
    }
}
