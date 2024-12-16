package in.koreatech.koin.domain.community.article.model.redis;

import in.koreatech.koin.domain.community.article.model.Article;
import org.springframework.data.annotation.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "busNoticeArticle")
public class BusNoticeArticle {

    @Id
    private Integer id;

    private String title;

    @Builder
    private BusNoticeArticle(Integer id, String title) {
        this.id = id;
        this.title = title;
    }

    public static BusNoticeArticle from(Article article) {
        return BusNoticeArticle.builder()
                .id(article.getId())
                .title(article.getTitle())
                .build();
    }
}
