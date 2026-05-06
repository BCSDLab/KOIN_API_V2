package in.koreatech.koin.domain.community.keyword.model;

import in.koreatech.koin.domain.community.keyword.enums.KeywordCategory;
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
    private KeywordCategory category;
    private Integer count;

    @Builder
    private ArticleKeywordSuggestCache(
        Integer hotKeywordId,
        String keyword,
        KeywordCategory category,
        Integer count
    ) {
        this.hotKeywordId = hotKeywordId;
        this.keyword = keyword;
        this.category = category != null ? category : KeywordCategory.KOREATECH;
        this.count = count;
    }
}
