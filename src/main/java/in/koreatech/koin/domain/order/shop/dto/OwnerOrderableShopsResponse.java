package in.koreatech.koin.domain.order.shop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OwnerOrderableShopsResponse(
    @Schema(description = "주문 가능 상점 수", example = "3", requiredMode = REQUIRED)
    Integer totalCount,

    @Schema(description = "주문 가능 상점 목록", requiredMode = REQUIRED)
    List<InnerOrderableShopResponse> shops
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerOrderableShopResponse(
        @Schema(description = "주문 가능 상점 ID", example = "1", requiredMode = REQUIRED)
        Integer orderableShopId,

        @Schema(description = "상점 ID", example = "14", requiredMode = REQUIRED)
        Integer shopId,

        @Schema(description = "상점 이름", example = "한끼반점 신전점", requiredMode = REQUIRED)
        String name,

        @Schema(description = "상점 주소", example = "천안시 동남구 병천면 충절로 1580", requiredMode = NOT_REQUIRED)
        String address,

        @Schema(description = "현재 영업 여부", example = "true", requiredMode = REQUIRED)
        Boolean isOpen
    ) {
        public static InnerOrderableShopResponse from(OrderableShop orderableShop) {
            Shop shop = orderableShop.getShop();

            return new InnerOrderableShopResponse(
                orderableShop.getId(),
                shop.getId(),
                shop.getName(),
                shop.getAddress(),
                orderableShop.isOpen()
            );
        }
    }

    public static OwnerOrderableShopsResponse from(List<OrderableShop> orderableShops) {
        List<InnerOrderableShopResponse> shops = orderableShops.stream()
            .map(InnerOrderableShopResponse::from)
            .toList();

        return new OwnerOrderableShopsResponse(shops.size(), shops);
    }
}
