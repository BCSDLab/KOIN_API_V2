package in.koreatech.koin.common.event;

import java.util.List;
import java.util.Map;

public record LostItemKeywordEvent(
    Integer articleId,
    String articleTitle,
    Integer authorId,
    MatchedKeywordUsers matchedKeywordUsers
) {
    public record MatchedKeywordUsers(
        Map<String, List<Integer>> userIdsByKeyword
    ) {

    }

    public static LostItemKeywordEvent of(
        Integer articleId,
        String articleTitle,
        Integer authorId,
        Map<String, List<Integer>> userIdsByKeyword
    ) {
        return new LostItemKeywordEvent(
            articleId,
            articleTitle,
            authorId,
            new MatchedKeywordUsers(userIdsByKeyword)
        );
    }
}
