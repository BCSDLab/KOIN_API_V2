package in.koreatech.koin.domain.ownershop.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.Shop;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OwnerShopsResponse(
    Integer count,
    List<InnerShopResponse> shops
) {

    public static OwnerShopsResponse from(List<InnerShopResponse> shops) {
        return new OwnerShopsResponse(shops.size(), shops);
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerShopResponse(
        Integer id,
        String name,
        boolean isEvent
    ) {

        public static InnerShopResponse from(Shop shop, boolean isEvent) {
            return new InnerShopResponse(shop.getId(), shop.getName(), isEvent);
        }
    }
}
