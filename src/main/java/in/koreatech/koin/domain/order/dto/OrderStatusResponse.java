package in.koreatech.koin.domain.order.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.model.Order;
import in.koreatech.koin.domain.order.model.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record OrderStatusResponse(
    @Schema(description = "주문 ID", example = "1", requiredMode = REQUIRED)
    Integer orderId,

    @Schema(description = "주문 타입", example = "TAKEOUT", requiredMode = REQUIRED)
    String orderType,

    @Schema(description = "주문 상태", example = "COOKING", requiredMode = REQUIRED)
    String orderStatus,

    @Schema(description = "예상 시각", example = "17:45", requiredMode = REQUIRED)
    @JsonFormat(pattern = "HH:mm")
    LocalTime estimatedAt,

    @Schema(description = "상점 이름", example = "코인 병천점", requiredMode = REQUIRED)
    String shopName
) {

    public static OrderStatusResponse from(Order order) {

        LocalTime estimatedTime = null;
        if (order.getOrderType() == OrderType.DELIVERY) {
            estimatedTime = LocalTime.from(order.getOrderDelivery().getEstimatedArrivalAt());
        } else if (order.getOrderType() == OrderType.TAKE_OUT) {
            estimatedTime = LocalTime.from(order.getOrderTakeout().getEstimatedPackagedAt());
        }

        return new OrderStatusResponse(
            order.getId(),
            order.getOrderType().name(),
            order.getStatus().name(),
            estimatedTime,
            order.getOrderableShopName());
    }
}
