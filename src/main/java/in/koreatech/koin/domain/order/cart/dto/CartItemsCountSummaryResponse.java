package in.koreatech.koin.domain.order.cart.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.cart.model.Cart;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CartItemsCountSummaryResponse(

    @Schema(description = "장바구니에 담긴 상품 종류 개수", example = "2")
    Integer itemTypeCount,
    @Schema(description = "장바구니에 담긴 상품의 총 수량", example = "7")
    Integer totalQuantity
) {

    public static CartItemsCountSummaryResponse from(Cart cart) {
        return new CartItemsCountSummaryResponse(cart.getItemTypeCount(), cart.getTotalQuantity());
    }

    public static CartItemsCountSummaryResponse empty() {
        return new CartItemsCountSummaryResponse(0, 0);
    }
}
