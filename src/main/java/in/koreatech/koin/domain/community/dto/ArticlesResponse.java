package in.koreatech.koin.domain.community.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.global.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ArticlesResponse(
    @Schema(description = "게시글 목록")
    List<InnerArticleResponse> articles,

    @Schema(description = "총 게시글 수", example = "57", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "현재 페이지에 포함된 게시글 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "총 페이지 수", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage
) {

    public static ArticlesResponse of(Page<Article> pagedResult, Criteria criteria) {
        return new ArticlesResponse(
            pagedResult.stream()
                .map(InnerArticleResponse::from)
                .toList(),
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerArticleResponse(

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

        public static InnerArticleResponse from(Article article) {
            return new InnerArticleResponse(
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
}
