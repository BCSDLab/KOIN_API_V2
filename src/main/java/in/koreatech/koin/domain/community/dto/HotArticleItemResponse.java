package in.koreatech.koin.domain.community.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.model.Article;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record HotArticleItemResponse(
    @Schema(description = "게시글 고유 ID", example = "1")
    Integer id,

    @Schema(description = "게시판 고유 ID", example = "1")
    Long boardId,

    @Schema(description = "제목", example = "제목")
    String title,

    @Schema(description = "내용 요약", example = "내용 요약")
    @JsonProperty("contentSummary") String contentSummary,

    @Schema(description = "댓글 수", example = "1")
    Byte commentCount,

    @Schema(description = "조회수", example = "1")
    Long hit,

    @Schema(description = "생성 일자", example = "2023-01-04 12:00:01")
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
