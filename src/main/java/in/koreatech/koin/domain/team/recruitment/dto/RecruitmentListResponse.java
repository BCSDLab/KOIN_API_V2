package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record RecruitmentListResponse(
    @Schema(description = "모집글 카드 목록", requiredMode = REQUIRED)
    List<RecruitmentCard> recruitments,

    @Schema(description = "전체 모집글 수", example = "20", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "현재 페이지의 모집글 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "전체 페이지 수", example = "2", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "1", requiredMode = REQUIRED)
    Integer currentPage
) {
    public static RecruitmentListResponse of(
        Page<TeamRecruitment> pagedResult,
        Criteria criteria,
        LocalDate today
    ) {
        return new RecruitmentListResponse(
            pagedResult.getContent().stream()
                .map(recruitment -> RecruitmentCards.of(recruitment, today))
                .toList(),
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1
        );
    }
}
