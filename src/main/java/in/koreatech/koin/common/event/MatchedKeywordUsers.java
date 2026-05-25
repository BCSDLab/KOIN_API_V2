package in.koreatech.koin.common.event;

import java.util.List;
import java.util.Map;

public record MatchedKeywordUsers(
    Map<Integer, String> keywordByUserId
) {
    public static MatchedKeywordUsers from(Map<Integer, String> keywordByUserId) {
        return new MatchedKeywordUsers(Map.copyOf(keywordByUserId));
    }

    public List<Integer> getMatchedUserIds() {
        return List.copyOf(keywordByUserId.keySet());
    }

    public String getKeyword(Integer userId) {
        return keywordByUserId.get(userId);
    }
}
