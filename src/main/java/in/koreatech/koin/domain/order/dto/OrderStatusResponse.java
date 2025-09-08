package in.koreatech.koin.domain.order.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.model.Order;
import in.koreatech.koin.domain.order.model.OrderType;
import in.koreatech.koin.domain.order.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record OrderStatusResponse(
    @Schema(description = "주문 ID", example = "1", requiredMode = REQUIRED)
    Integer orderId,

    @Schema(description = "주문 타입", example = "TAKEOUT", requiredMode = REQUIRED)
    String orderType,

    @Schema(description = "주문 상태", example = "COOKING", requiredMode = REQUIRED)
    String orderStatus,

    @Schema(description = "예상 시각", example = "2025-09-07T17:45:00", requiredMode = REQUIRED)
    @JsonFormat(pattern = "HH:mm")
    LocalDateTime estimatedAt,

    @Schema(description = "상점 이름", example = "코인 병천점", requiredMode = REQUIRED)
    String shopName
) {

    public static OrderStatusResponse from(Order order) {

        LocalDateTime eta = null;
        if (order.getOrderType() == OrderType.DELIVERY) {
            eta = order.getOrderDelivery().getEstimatedArrivalAt();
        } else if (order.getOrderType() == OrderType.TAKE_OUT) {
            eta = order.getOrderTakeout().getEstimatedPackagedAt();
        }

        return new OrderStatusResponse(
            order.getId(),
            order.getOrderType().name(),
            order.getStatus().name(),
            eta,
            order.getOrderableShopName());
    }
}
