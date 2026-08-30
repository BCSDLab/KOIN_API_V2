package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record CreatedRecruitmentListResponse(
    @Schema(description = "내가 작성한 모집글 목록", requiredMode = REQUIRED)
    List<CreatedRecruitment> recruitments,

    @Schema(description = "전체 모집글 수", example = "1", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "현재 페이지의 모집글 수", example = "1", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "전체 페이지 수", example = "1", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "1", requiredMode = REQUIRED)
    Integer currentPage
) {
    public static CreatedRecruitmentListResponse of(
        List<CreatedRecruitment> recruitments,
        Page<?> pagedResult,
        Criteria criteria
    ) {
        return new CreatedRecruitmentListResponse(
            recruitments,
            pagedResult.getTotalElements(),
            recruitments.size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1
        );
    }
}
