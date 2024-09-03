package in.koreatech.koin.domain.community.article.model.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import in.koreatech.koin.domain.community.article.model.Article;
import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("articleHit")
public class ArticleHit {

    @Id
    private Integer id;

    private Integer hit;

    @Builder
    private ArticleHit(Integer id, Integer hit) {
        this.id = id;
        this.hit = hit;
    }

    public static ArticleHit from(Article article) {
        return ArticleHit.builder()
            .id(article.getId())
            .hit(article.getHit())
            .build();
    }
}
