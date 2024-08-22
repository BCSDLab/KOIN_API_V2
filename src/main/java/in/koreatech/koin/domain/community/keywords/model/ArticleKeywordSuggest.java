package in.koreatech.koin.domain.community.keywords.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("HotArticleKeyword")
public class ArticleKeywordSuggest {

    @Id
    private Integer hotKeywordId;
    private String keyword;
    private Integer count;

    @Builder
    public ArticleKeywordSuggest(Integer hotKeywordId, String keyword, Integer count) {
        this.hotKeywordId = hotKeywordId;
        this.keyword = keyword;
        this.count = count;
    }
}
