package in.koreatech.koin.domain.community.keyword.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("HotArticleKeyword")
public class ArticleKeywordSuggestCache {

    @Id
    private Integer hotKeywordId;
    private String keyword;
    private Integer count;

    @Builder
    public ArticleKeywordSuggestCache(Integer hotKeywordId, String keyword, Integer count) {
        this.hotKeywordId = hotKeywordId;
        this.keyword = keyword;
        this.count = count;
    }
}
