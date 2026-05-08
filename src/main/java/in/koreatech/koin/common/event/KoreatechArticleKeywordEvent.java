package in.koreatech.koin.common.event;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record KoreatechArticleKeywordEvent(
    Integer articleId,
    Integer boardId,
    String articleTitle,
    MatchedKeywordUsers matchedKeywordUsers
) {

    public KoreatechArticleKeywordEvent {
        matchedKeywordUsers = new MatchedKeywordUsers(matchedKeywordUsers.userIdsByKeyword());
    }

    public record MatchedKeywordUsers(
        Map<String, List<Integer>> userIdsByKeyword
    ) {

        public MatchedKeywordUsers {
            Map<String, List<Integer>> copiedUserIdsByKeyword = new LinkedHashMap<>();
            userIdsByKeyword.forEach((keyword, userIds) ->
                copiedUserIdsByKeyword.put(keyword, List.copyOf(userIds))
            );
            userIdsByKeyword = Collections.unmodifiableMap(copiedUserIdsByKeyword);
        }
    }
}
