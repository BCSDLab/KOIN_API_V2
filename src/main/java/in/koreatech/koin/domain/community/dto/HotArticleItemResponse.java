package in.koreatech.koin.domain.community.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.model.Article;

@JsonNaming(SnakeCaseStrategy.class)
public record HotArticleItemResponse(
    Long id,
    Long boardId,
    String title,
    @JsonProperty("contentSummary") String contentSummary,
    Byte commentCount,
    Long hit,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt
) {

    public static HotArticleItemResponse from(Article article) {
        return new HotArticleItemResponse(
            article.getId(),
            article.getBoard().getId(),
            article.getTitle(),
            article.getContentSummary(),
            article.getCommentCount(),
            article.getHit(),
            article.getCreatedAt()
        );
    }
}
