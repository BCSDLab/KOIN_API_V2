package in.koreatech.koin.domain.community.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import in.koreatech.koin.domain.community.model.Article;

public record HotArticleItemResponse(
    Long id,
    Long board_id,
    String title,
    String contentSummary,
    Long comment_count,
    Long hit,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt
) {

    public static HotArticleItemResponse from(Article article) {
        return new HotArticleItemResponse(
            article.getId(),
            article.getBoard().getId(),
            article.getTitle(),
            article.getContentSummary(),
            (long)article.getComment().size(),
            article.getHit(),
            article.getCreatedAt()
        );
    }
}
