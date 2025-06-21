package in.koreatech.koin.domain.order.cart.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import in.koreatech.koin.domain.order.cart.model.Cart;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CartPaymentSummaryResponse(
    @Schema(description = "장바구니에 담긴 상품의 총 금액 (배달비 제외)", example = "25000")
    Integer itemTotalAmount,
    @Schema(description = "배달비", example = "3000")
    Integer deliveryFee,
    @Schema(description = "상품 금액과 배달비를 합한 총 금액", example = "28000")
    Integer totalAmount,
    @Schema(description = "결제 예정 금액 (할인 등을 반영한 최종 금액)", example = "28000")
    Integer finalPaymentAmount
) {

    public static CartPaymentSummaryResponse from(Cart cart) {
        Integer itemTotalAmount = cart.calculateItemsAmount();
        Integer deliveryFee = cart.getOrderableShop().calculateDeliveryFee(itemTotalAmount);
        return new CartPaymentSummaryResponse(
            itemTotalAmount,
            deliveryFee,
            cart.calculateItemsAmount() + deliveryFee,
            cart.calculateItemsAmount() + deliveryFee // 할인 정책 없으므로 totalAmount와 동일한 금액 반환
        );
    }

    public static CartPaymentSummaryResponse empty() {
        return new CartPaymentSummaryResponse(0, 0, 0, 0);
    }
}
