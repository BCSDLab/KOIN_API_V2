package in.koreatech.koin.domain.order.order.dto.response;

import static in.koreatech.koin.domain.order.order.model.OrderStatus.CONFIRMING;
import static in.koreatech.koin.domain.order.order.model.OrderType.DELIVERY;
import static in.koreatech.koin.domain.order.order.model.OrderType.TAKE_OUT;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.payment.model.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record InprogressOrderResponse(
    @Schema(description = "주문 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "주문 가능 상점 ID", example = "14", requiredMode = REQUIRED)
    Integer orderableShopId,

    @Schema(description = "결제 ID", example = "1", requiredMode = REQUIRED)
    Integer paymentId,

    @Schema(description = "주문 타입", example = "TAKEOUT", requiredMode = REQUIRED)
    String orderType,

    @Schema(description = "상점 이름", example = "코인 병천점", requiredMode = REQUIRED)
    String orderableShopName,

    @Schema(description = "상점 썸네일 URL", example = "https://static.koreatech.in/test.png", requiredMode = REQUIRED)
    String orderableShopThumbnail,

    @Schema(description = "예상 시각", example = "17:45", requiredMode = NOT_REQUIRED)
    @JsonFormat(pattern = "HH:mm")
    LocalTime estimatedAt,

    @Schema(description = "주문 상태", example = "COOKING", requiredMode = REQUIRED)
    String orderStatus,

    @Schema(description = "주문 내용", example = "족발 외 1건", requiredMode = REQUIRED)
    String orderTitle,

    @Schema(description = "총 금액", example = "50000", requiredMode = REQUIRED)
    Integer totalAmount
) {
    public static InprogressOrderResponse from(Order order, Payment payment) {

        LocalTime estimatedTime = null;
        if (order.getStatus() != CONFIRMING) {
            if (order.getOrderType() == DELIVERY) {
                estimatedTime = LocalTime.from(order.getOrderDelivery().getEstimatedArrivalAt());
            } else if (order.getOrderType() == TAKE_OUT) {
                estimatedTime = LocalTime.from(order.getOrderTakeout().getEstimatedPackagedAt());
            }
        }

        return new InprogressOrderResponse(
            order.getId(),
            order.getOrderableShop().getId(),
            payment.getId(),
            order.getOrderType().name(),
            order.getOrderableShopName(),
            order.getOrderableShop().getThumbnailImage(),
            estimatedTime,
            order.getStatus().name(),
            payment.getDescription(),
            order.getTotalPrice()
        );
    }
}
