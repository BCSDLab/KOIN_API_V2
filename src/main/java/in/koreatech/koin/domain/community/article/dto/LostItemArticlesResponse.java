package in.koreatech.koin.domain.community.article.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin._common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LostItemArticlesResponse(
    @Schema(description = "분실물 게시글 목록")
    List<InnerLostItemArticleResponse> articles,

    @Schema(description = "총 게시글 수", example = "57", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "현재 페이지에 포함된 게시글 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "총 페이지 수", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage
) {

    public static LostItemArticlesResponse of(Page<Article> pagedResult, Criteria criteria, Integer userId) {
        return new LostItemArticlesResponse(
            pagedResult.stream()
                .map((Article article) -> InnerLostItemArticleResponse.of(article, userId))
                .toList(),
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerLostItemArticleResponse(
        @Schema(description = "게시글 id", example = "17368", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "게시판 id", example = "14", requiredMode = REQUIRED)
        Integer boardId,

        @Schema(description = "게시글 타입", example = "LOST", requiredMode = NOT_REQUIRED)
        String type,

        @Schema(description = "분실물 종류", example = "신분증", requiredMode = REQUIRED)
        String category,

        @Schema(description = "습득 장소", example = "학생회관 앞", requiredMode = REQUIRED)
        String foundPlace,

        @Schema(description = "습득 날짜", example = "2025-01-01", requiredMode = REQUIRED)
        LocalDate foundDate,

        @Schema(description = "본문", example = "학생회관 앞 계단에 …")
        String content,

        @Schema(description = "작성자", example = "총학생회", requiredMode = REQUIRED)
        String author,

        @Schema(description = "등록일", example = "2025-01-10", requiredMode = REQUIRED)
        LocalDate registeredAt,

        @Schema(description = "처리되지 않은 자신의 신고 존재 여부", example = "true", requiredMode = REQUIRED)
        Boolean isReported
    ) {

        public static InnerLostItemArticleResponse of(Article article, Integer userId) {
            LostItemArticle lostItemArticle = article.getLostItemArticle();
            return new InnerLostItemArticleResponse(
                article.getId(),
                article.getBoard().getId(),
                lostItemArticle.getType(),
                lostItemArticle.getCategory(),
                lostItemArticle.getFoundPlace(),
                lostItemArticle.getFoundDate(),
                article.getContent(),
                article.getAuthor(),
                article.getRegisteredAt(),
                lostItemArticle.isReportedByUserId(userId)
            );
        }
    }
}
