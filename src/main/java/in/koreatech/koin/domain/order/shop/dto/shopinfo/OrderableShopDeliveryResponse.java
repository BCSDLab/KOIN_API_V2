package in.koreatech.koin.domain.order.shop.dto.shopinfo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OrderableShopDeliveryResponse(
    @Schema(description = "상점 교내 배달 가능 여부", example = "true")
    Boolean campusDelivery,
    @Schema(description = "상점 교외 배달 가능 여부", example = "true")
    Boolean offCampusDelivery
) {
    public static OrderableShopDeliveryResponse from(OrderableShop orderableShop) {
        return new OrderableShopDeliveryResponse(
            orderableShop.getDeliveryOption().getCampusDelivery(),
            orderableShop.getDeliveryOption().getOffCampusDelivery()
        );
    }
}
