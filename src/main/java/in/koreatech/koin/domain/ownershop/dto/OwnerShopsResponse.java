package in.koreatech.koin.domain.ownershop.dto;

import java.util.List;

import in.koreatech.koin.domain.owner.domain.Owner;
import in.koreatech.koin.domain.shop.model.Shop;

public record OwnerShopsResponse(
    Long count,
    List<InnerShopResponse> shops
) {

    public static OwnerShopsResponse from(Owner owner) {
        return new OwnerShopsResponse(
            (long)owner.getShops().size(),
            owner.getShops().stream()
                .map(InnerShopResponse::from)
                .toList()
        );
    }

    private record InnerShopResponse(
        Long id,
        String name
    ) {

        public static InnerShopResponse from(Shop shop) {
            return new InnerShopResponse(shop.getId(), shop.getName());
        }
    }
}
