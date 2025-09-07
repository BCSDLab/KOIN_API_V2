package in.koreatech.koin.domain.order.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.model.Order;
import in.koreatech.koin.domain.order.model.OrderType;
import in.koreatech.koin.domain.order.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record OrderStatusResponse(
    @Schema(description = "주문 ID")
    Integer orderId,

    @Schema(description = "주문 타입")
    String orderType,

    @Schema(description = "주문 상태")
    String orderStatus,

    @Schema(description = "예상 시각")
    LocalDateTime estimatedAt,

    @Schema(description = "상점 이름")
    String shopName
) {

    public static OrderStatusResponse from(Order order) {
        String type = null;
        OrderType orderType = order.getOrderType();
        if (orderType != null) type = orderType.name();

        String statusLabel = null;
        OrderStatus status = order.getStatus();
        if (status != null) {
            statusLabel = status.name();
        }

        LocalDateTime eta = null;
        if (order.getOrderDelivery() != null) {
            eta = order.getOrderDelivery().getEstimatedArrivalAt();
        } else if (order.getOrderTakeout() != null) {
            eta = order.getOrderTakeout().getEstimatedPackagedAt();
        }

        String shopName = order.getOrderableShopName();

        return new OrderStatusResponse(order.getId(), type, statusLabel, eta, shopName);
    }
}
