package in.koreatech.koin.domain.order.cart.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.cart.model.Cart;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CartAmountSummaryResponse(
    @Schema(description = "주문 가능 상점 ID", example = "1")
    Integer orderableShopId,
    @Schema(description = "상점의 최소 주문 금액", example = "15000")
    Integer shopMinimumOrderAmount,
    @Schema(description = "상품 총 금액 (배달비 제외)", example = "18000")
    Integer cartItemsAmount,
    @Schema(description = "바텀 시트 활성화 여부", example = "true")
    Boolean isAvailable
) {

    public static CartAmountSummaryResponse from(Cart cart) {
        return new CartAmountSummaryResponse(
            cart.getOrderableShop().getId(),
            cart.getOrderableShop().getMinimumOrderAmount(),
            cart.calculateItemsAmount(),
            true
        );
    }

    public static CartAmountSummaryResponse empty() {
        return new CartAmountSummaryResponse(0, 0, 0, false);
    }
}
