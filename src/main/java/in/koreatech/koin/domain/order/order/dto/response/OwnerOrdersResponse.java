package in.koreatech.koin.domain.order.order.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.order.model.Order;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OwnerOrdersResponse(
    @Schema(description = "조회된 주문 수", example = "3", requiredMode = REQUIRED)
    Integer totalCount,

    @Schema(description = "주문 목록", requiredMode = REQUIRED)
    List<InnerOwnerOrderResponse> orders
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerOwnerOrderResponse(
        @Schema(description = "주문 고유 ID", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "주문 번호", example = "A1B2C3D4E5", requiredMode = REQUIRED)
        String orderNumber,

        @Schema(description = "주문 유형", example = "DELIVERY", requiredMode = REQUIRED)
        String orderType,

        @Schema(description = "주문 상태", example = "CONFIRMING", requiredMode = REQUIRED)
        String orderStatus,

        @Schema(description = "주문 접수 일시", example = "2026-09-20 18:42:00", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime orderedAt,

        @Schema(description = "배달은 도착 예정, 포장은 포장 완료 예정 일시", example = "2026-09-20 19:02:00",
            requiredMode = NOT_REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime estimatedAt,

        @Schema(description = "총 결제 금액", example = "35000", requiredMode = REQUIRED)
        Integer totalPrice
    ) {
        public static InnerOwnerOrderResponse from(Order order) {
            return new InnerOwnerOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderType().name(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getEstimatedAt(),
                order.getTotalPrice()
            );
        }
    }

    public static OwnerOrdersResponse from(List<Order> orders) {
        List<InnerOwnerOrderResponse> orderResponses = orders.stream()
            .map(InnerOwnerOrderResponse::from)
            .toList();

        return new OwnerOrdersResponse(orderResponses.size(), orderResponses);
    }
}
