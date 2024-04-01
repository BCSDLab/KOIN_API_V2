package in.koreatech.koin.domain.ownershop.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.Shop;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OwnerShopsResponse(
    Long count,
    List<InnerShopResponse> shops
) {

    public static OwnerShopsResponse from(List<Shop> shops) {
        return new OwnerShopsResponse(
            (long)shops.size(),
            shops.stream()
                .map(InnerShopResponse::from)
                .toList()
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerShopResponse(
        Long id,
        String name
    ) {

        public static InnerShopResponse from(Shop shop) {
            return new InnerShopResponse(shop.getId(), shop.getName());
        }
    }
}
