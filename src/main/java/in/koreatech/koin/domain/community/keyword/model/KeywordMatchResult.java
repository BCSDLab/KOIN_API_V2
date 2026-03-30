package in.koreatech.koin.domain.community.keyword.model;

import java.util.Objects;

public class KeywordMatchResult {

    private final Integer articleId;
    private final Integer userId;
    private String keyword;

    private KeywordMatchResult(Integer articleId, Integer userId, String keyword) {
        this.articleId = articleId;
        this.userId = userId;
        this.keyword = keyword;
    }

    public static KeywordMatchResult of(Integer articleId, Integer userId, String keyword) {
        return new KeywordMatchResult(articleId, userId, keyword);
    }

    public void updateKeywordIfLonger(String candidate) {
        if (candidate.trim().length() > this.keyword.trim().length()) {
            this.keyword = candidate;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeywordMatchResult that)) return false;
        return Objects.equals(articleId, that.articleId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articleId, userId);
    }
}
