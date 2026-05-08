package in.koreatech.koin.common.event;

import java.util.List;
import java.util.Map;

public record KoreatechArticleKeywordEvent(
    Integer articleId,
    Integer boardId,
    String articleTitle,
    MatchedKeywordUsers matchedKeywordUsers
) {
    public record MatchedKeywordUsers(
        Map<String, List<Integer>> userIdsByKeyword
    ) {

    }

    public static KoreatechArticleKeywordEvent of(
        Integer articleId,
        Integer boardId,
        String articleTitle,
        Map<String, List<Integer>> userIdsByKeyword
    ) {
        return new KoreatechArticleKeywordEvent(
            articleId,
            boardId,
            articleTitle,
            new MatchedKeywordUsers(userIdsByKeyword)
        );
    }
}
