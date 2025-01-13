package in.koreatech.koin.domain.community.article.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

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

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerLostItemArticleResponse(
        @Schema(description = "게시글 id", example = "17368", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "게시판 id", example = "14", requiredMode = REQUIRED)
        Integer boardId,

        @Schema(description = "분실물 종류", example = "신분증", requiredMode = REQUIRED)
        String category,

        @Schema(description = "습득 장소", example = "학생회관 앞", requiredMode = REQUIRED)
        String location,

        @Schema(description = "습득 날짜", example = "2025-01-01", requiredMode = REQUIRED)
        LocalDate foundDate,

        @Schema(description = "본문", example = "학생회관 앞 계단에 …")
        String content,

        @Schema(description = "작성자", example = "총학생회", requiredMode = REQUIRED)
        String author,

        @Schema(description = "이전글 id", example = "17367")
        Integer prevId,

        @Schema(description = "다음글 id", example = "17369")
        Integer nextId,

        @Schema(description = "등록일", example = "2025-01-10", requiredMode = REQUIRED)
        LocalDate registeredAt
    ) {

    }

}
