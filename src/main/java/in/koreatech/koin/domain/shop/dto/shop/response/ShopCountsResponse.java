package in.koreatech.koin.domain.shop.dto.shop.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopCountsResponse(
    @Schema(example = "24", description = "전체 상점 개수", requiredMode = REQUIRED)
    Integer totalCount,

    @Schema(example = "12", description = "현재 영업 중인 상점 개수", requiredMode = REQUIRED)
    Integer openCount
) {
}
