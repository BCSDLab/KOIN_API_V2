package in.koreatech.koin.domain.order.shop.dto.menu;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OrderableShopMenuPricesResponse (
    @Schema(description = "메뉴 가격 옵션 고유 식별자", example = "1", nullable = true)
    Integer id,
    @Schema(description = "메뉴 가격 옵션 이름", example = "2마리", nullable = true)
    String name,
    @Schema(description = "메뉴 가격", example = "36000")
    Integer price
) {

    public static List<OrderableShopMenuPricesResponse> from(OrderableShopMenu menu) {
        return menu.getMenuPrices().stream().map(OrderableShopMenuPricesResponse::from)
            .collect(Collectors.toList());
    }

    private static OrderableShopMenuPricesResponse from(OrderableShopMenuPrice menuPrice) {
        return new OrderableShopMenuPricesResponse(menuPrice.getId(), menuPrice.getName(), menuPrice.getPrice());
    }
}
