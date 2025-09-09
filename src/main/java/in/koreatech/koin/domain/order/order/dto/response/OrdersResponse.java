package in.koreatech.koin.domain.order.order.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.order.model.OrderInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OrdersResponse(
    @Schema(description = "주문 내역 수", example = "10", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "현재 페이지에서 조회된 주문 내역 수", example = "5", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "최대 페이지", example = "2", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "1", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "주문 내역 리스트", requiredMode = REQUIRED)
    List<InnerOrderResponse> orders
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerOrderResponse(
        @Schema(description = "주문 고유 Id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "결제 고유 Id", example = "1", requiredMode = REQUIRED)
        Integer paymentId,

        @Schema(description = "주문 가능 상점 ID", example = "1", requiredMode = REQUIRED)
        Integer orderableShopId,

        @Schema(description = "주문 가게 이름", example = "김밥 천국", requiredMode = REQUIRED)
        String orderableShopName,

        @Schema(description = "주문 일시", example = "2025.09.07", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy.MM.dd")
        LocalDate orderDate,

        @Schema(description = "주문 상태", example = "DELIVERED", requiredMode = REQUIRED)
        String orderStatus,

        @Schema(description = "주문 내용", example = "김밥 외 1건", requiredMode = REQUIRED)
        String orderTitle
    ) {
        public static InnerOrderResponse from(OrderInfo orderInfo) {
            return new InnerOrderResponse(
                orderInfo.orderId(),
                orderInfo.paymentId(),
                orderInfo.orderShopId(),
                orderInfo.orderableShopName(),
                orderInfo.orderDate().toLocalDate(),
                orderInfo.orderStatus().name(),
                orderInfo.orderTitle()
            );
        }
    }

    public static OrdersResponse of(Page<OrderInfo> orderInfoPage) {
        return new OrdersResponse(
            orderInfoPage.getTotalElements(),
            orderInfoPage.getContent().size(),
            orderInfoPage.getTotalPages(),
            orderInfoPage.getNumber() + 1,
            orderInfoPage.stream()
                .map(InnerOrderResponse::from)
                .toList()
        );
    }
}
