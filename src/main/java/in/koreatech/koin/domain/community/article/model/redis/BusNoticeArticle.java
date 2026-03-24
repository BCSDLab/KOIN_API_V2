package in.koreatech.koin.domain.community.article.model.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Builder;
import lombok.Getter;

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

    public static BusNoticeArticle of(int id, String title) {
        return BusNoticeArticle.builder()
            .id(id)
            .title(title)
            .build();
    }
}
