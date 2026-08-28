package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record MyApplicationListResponse(
    @Schema(description = "내 지원 목록", requiredMode = REQUIRED)
    List<MyApplication> applications,

    @Schema(description = "전체 결과 수", example = "1", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "현재 페이지 결과 수", example = "1", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "전체 페이지 수", example = "1", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "1", requiredMode = REQUIRED)
    Integer currentPage
) {
}
