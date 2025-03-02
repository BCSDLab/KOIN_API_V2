package in.koreatech.koin.domain.community.article.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin._common.model.Criteria;
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

    public static ArticlesResponse of(Page<Article> pagedResult, Criteria criteria, Integer userId) {
        return new ArticlesResponse(
            pagedResult.stream()
                .map(article -> InnerArticleResponse.of(article, userId))
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

        @Schema(description = "작성자", example = "닉네임", requiredMode = REQUIRED)
        String author,

        @Schema(description = "조회수", example = "1", requiredMode = REQUIRED)
        int hit,

        @Schema(description = "등록 일자", example = "2024-08-28", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate registeredAt,

        @Schema(description = "수정 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt,

        @Schema(description = "처리되지 않은 자신의 신고 존재 여부", example = "true", requiredMode = REQUIRED)
        Boolean isReported
    ) {

        public static InnerArticleResponse of(Article article, Integer userId) {
            Optional<LostItemArticle> lostItemArticle = Optional.ofNullable(article.getLostItemArticle());
            return new InnerArticleResponse(
                article.getId(),
                article.getBoard().getId(),
                article.getTitle(),
                article.getAuthor(),
                article.getTotalHit(),
                article.getRegisteredAt(),
                article.getUpdatedAt(),
                lostItemArticle.map(lostItem -> lostItem.isReportedByUserId(userId)).orElse(false)
            );
        }
    }
}
