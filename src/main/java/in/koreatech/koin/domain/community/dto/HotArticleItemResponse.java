package in.koreatech.koin.domain.community.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.model.Article;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record HotArticleItemResponse(
    @Schema(description = "게시글 고유 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "게시판 고유 ID", example = "1", requiredMode = REQUIRED)
    Integer boardId,

    @Schema(description = "제목", example = "제목", requiredMode = REQUIRED)
    String title,

    @Schema(description = "내용 요약", example = "내용 요약", requiredMode = NOT_REQUIRED)
    @JsonProperty("contentSummary")
    String contentSummary,

    @Schema(description = "댓글 수", example = "1", requiredMode = REQUIRED)
    Byte commentCount,

    @Schema(description = "조회수", example = "1", requiredMode = REQUIRED)
    Integer hit,

    @Schema(description = "생성 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt
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
