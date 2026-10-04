package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record DiningReportPageResponse<T>(
    @Schema(description = "현재 페이지의 제보 목록. 조회 결과가 없으면 빈 배열입니다.", requiredMode = REQUIRED)
    List<T> reports,
    @Schema(description = "조회 조건에 맞는 전체 제보 수", example = "2", minimum = "0", requiredMode = REQUIRED)
    Integer totalCount,
    @Schema(description = "현재 페이지의 제보 수", example = "2", minimum = "0", requiredMode = REQUIRED)
    Integer currentCount,
    @Schema(description = "전체 페이지 수. 조회 결과가 없어도 1입니다.", example = "1", minimum = "1", requiredMode = REQUIRED)
    Integer totalPage,
    @Schema(description = "보정된 현재 페이지 번호. 1부터 시작합니다.", example = "1", minimum = "1", requiredMode = REQUIRED)
    Integer currentPage
) {
    public static <T> DiningReportPageResponse<T> of(List<T> reports, int total, Criteria criteria) {
        return new DiningReportPageResponse<>(reports, total, reports.size(),
            Math.max(1, (int)Math.ceil((double)total / criteria.getLimit())), criteria.getPage() + 1);
    }
}
