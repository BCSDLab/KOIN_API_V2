package in.koreatech.koin.domain.community.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.model.Article;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record ArticleResponse(

    @Schema(description = "게시글 고유 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "게시판 고유 ID", example = "1", requiredMode = REQUIRED)
    Integer boardId,

    @Schema(description = "제목", example = "제목", requiredMode = REQUIRED)
    String title,

    @Schema(description = "내용", example = "내용", requiredMode = REQUIRED)
    String content,

    @Schema(description = "작성자 닉네임", example = "닉네임", requiredMode = REQUIRED)
    String nickname,

    @Schema(description = "조회수", example = "1", requiredMode = REQUIRED)
    int hit,

    @Schema(description = "생성 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,

    @Schema(description = "수정 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
) {

    public static ArticleResponse of(Article article) {
        return new ArticleResponse(
            article.getId(),
            article.getBoard().getId(),
            article.getTitle(),
            article.getContent(),
            article.getNickname(),
            article.getHit(),
            article.getCreatedAt(),
            article.getUpdatedAt()
        );
    }
}