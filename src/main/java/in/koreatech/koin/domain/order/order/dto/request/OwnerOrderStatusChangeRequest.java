package in.koreatech.koin.domain.order.order.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.order.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OwnerOrderStatusChangeRequest(
    @Schema(description = "변경할 주문 상태", example = "COOKING", requiredMode = REQUIRED)
    @NotNull(message = "변경할 주문 상태는 필수입니다.")
    OrderStatus status,

    @Schema(description = "예상 소요 시간 (분). 승인할 때만 사용한다.", example = "20", requiredMode = NOT_REQUIRED)
    @Positive(message = "예상 소요 시간은 양수여야 합니다.")
    Integer estimatedMinutes,

    @Schema(description = "반려 사유. 반려할 때만 사용한다.", example = "재료 소진", requiredMode = NOT_REQUIRED)
    @Size(max = 200, message = "반려 사유는 200자를 넘을 수 없습니다.")
    String canceledReason
) {

}
