package in.koreatech.koin.domain.order.order.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.order.dto.request.OwnerOrderStatusCriteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OwnerOrderCountsResponse(
    @Schema(description = "신규 주문 수", example = "3", requiredMode = REQUIRED)
    Long newCount,

    @Schema(description = "조리중 주문 수", example = "2", requiredMode = REQUIRED)
    Long cookingCount,

    @Schema(description = "배달중 주문 수", example = "1", requiredMode = REQUIRED)
    Long deliveringCount,

    @Schema(description = "완료 주문 수 (배달 완료, 반려 합계)", example = "12", requiredMode = REQUIRED)
    Long completedCount
) {
    public static OwnerOrderCountsResponse from(Map<OwnerOrderStatusCriteria, Long> countByCriteria) {
        return new OwnerOrderCountsResponse(
            countByCriteria.getOrDefault(OwnerOrderStatusCriteria.NEW, 0L),
            countByCriteria.getOrDefault(OwnerOrderStatusCriteria.COOKING, 0L),
            countByCriteria.getOrDefault(OwnerOrderStatusCriteria.DELIVERING, 0L),
            countByCriteria.getOrDefault(OwnerOrderStatusCriteria.COMPLETED, 0L)
        );
    }
}
