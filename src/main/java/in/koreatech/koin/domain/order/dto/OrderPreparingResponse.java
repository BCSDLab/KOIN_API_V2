package in.koreatech.koin.domain.order.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.model.Order;
import in.koreatech.koin.domain.order.model.OrderStatus;
import in.koreatech.koin.domain.order.model.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record OrderPreparingResponse(
    @Schema(description = "주문 ID")
    Integer orderId,

    @Schema(description = "주문 타입")
    String orderType,

    @Schema(description = "상점 이름")
    String shopName,

    @Schema(description = "상점 썸네일 URL")
    String shopThumbnail,

    @Schema(description = "예상 시각")
    LocalDateTime estimatedAt,

    @Schema(description = "주문 상태")
    String orderStatus,

    @Schema(description = "주문 내용")
    String paymentDescription
) {
    public static OrderPreparingResponse from(Order order, String paymentDescription) {

        String statusLabel = null;
        OrderStatus status = order.getStatus();
        if (status != null) {
            statusLabel = status.getDescription();
        }

        LocalDateTime eta = null;
        if (order.getOrderDelivery() != null) {
            eta = order.getOrderDelivery().getEstimatedArrivalAt();
        } else if (order.getOrderTakeout() != null) {
            eta = order.getOrderTakeout().getEstimatedPackagedAt();
        }

        String thumbnail = null;
        if (order.getOrderableShop() != null) {
            thumbnail = order.getOrderableShop().getThumbnailImage();
        }

        String shopName = order.getOrderableShopName();

        String type = null;
        OrderType orderType = order.getOrderType();
        if (orderType != null) {
            type = orderType.name();
        }

        return new OrderPreparingResponse(
            order.getId(),
            type,
            shopName,
            thumbnail,
            eta,
            statusLabel,
            paymentDescription
        );
    }
}
