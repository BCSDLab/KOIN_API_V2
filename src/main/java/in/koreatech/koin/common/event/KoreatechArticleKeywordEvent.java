package in.koreatech.koin.common.event;

import java.util.Map;

public record KoreatechArticleKeywordEvent(
    Integer articleId,
    Integer boardId,
    String articleTitle,
    MatchedKeywordUsers matchedKeywordUsers
) {

    public static KoreatechArticleKeywordEvent of(
        Integer articleId,
        Integer boardId,
        String articleTitle,
        Map<Integer, String> keywordByUserId
    ) {
        return new KoreatechArticleKeywordEvent(
            articleId,
            boardId,
            articleTitle,
            MatchedKeywordUsers.from(keywordByUserId)
        );
    }
}
