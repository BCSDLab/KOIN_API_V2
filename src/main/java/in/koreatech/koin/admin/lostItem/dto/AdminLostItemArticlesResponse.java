package in.koreatech.koin.admin.lostItem.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.domain.community.article.model.LostItemReport;
import in.koreatech.koin.domain.shop.model.review.ReportStatus;
import in.koreatech.koin.common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminLostItemArticlesResponse(
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

    public static AdminLostItemArticlesResponse of(Page<LostItemArticle> pagedResult, Criteria criteria) {
        return new AdminLostItemArticlesResponse(
            pagedResult.stream()
                .map(
                    AdminLostItemArticlesResponse.InnerLostItemArticleResponse::from)
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

        @Schema(description = "처리되지 않은 신고 존재 여부", example = "true", requiredMode = REQUIRED)
        Boolean isReported,

        @Schema(description = "처리되지 않은 신고 리스트")
        List<InnerReportResponse> unhandledReports
    ) {

        public static InnerLostItemArticleResponse from(LostItemArticle lostItemArticle) {
            Article article = lostItemArticle.getArticle();

            return new InnerLostItemArticleResponse(
                article.getId(),
                article.getBoard().getId(),
                lostItemArticle.getCategory(),
                lostItemArticle.getFoundPlace(),
                lostItemArticle.getFoundDate(),
                article.getContent(),
                article.getAuthor(),
                article.getRegisteredAt(),
                lostItemArticle.isReported(),
                lostItemArticle.getLostItemReports()
                    .stream()
                    .filter(report -> report.getReportStatus() == ReportStatus.UNHANDLED)
                    .map(InnerReportResponse::from)
                    .toList()
            );
        }

        public record InnerReportResponse(
            @Schema(example = "1", description = "신고 ID", requiredMode = REQUIRED)
            int reportId,

            @Schema(example = "부적절한 내용", description = "신고 제목", requiredMode = REQUIRED)
            String title,

            @Schema(example = "이 게시글은 욕설을 포함하고 있습니다.", description = "신고 내용", requiredMode = REQUIRED)
            String content,

            @Schema(example = "user1234", description = "신고자 닉네임", requiredMode = REQUIRED)
            String nickName,

            @Schema(example = "UNHANDLED", description = "신고 상태", requiredMode = REQUIRED)
            String status
        ) {

            public static InnerReportResponse from(LostItemReport report) {
                return new InnerReportResponse(
                    report.getId(),
                    report.getTitle(),
                    report.getContent(),
                    report.getStudent().getUser().getNickname(),
                    report.getReportStatus().name()
                );
            }
        }
    }
}
