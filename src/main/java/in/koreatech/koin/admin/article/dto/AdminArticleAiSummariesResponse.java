package in.koreatech.koin.admin.article.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminArticleAiSummariesResponse(
    @Schema(description = "게시글 AI 요약 작업 목록", requiredMode = REQUIRED)
    List<AdminArticleAiSummaryResponse> summaries,

    @Schema(description = "총 작업 수", example = "57", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "현재 페이지에 포함된 작업 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "총 페이지 수", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage
) {

    public static AdminArticleAiSummariesResponse of(
        Page<AdminArticleAiSummaryResponse> pagedResult,
        Criteria criteria
    ) {
        return new AdminArticleAiSummariesResponse(
            pagedResult.getContent(),
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1
        );
    }
}
