package in.koreatech.koin.domain.shop.cache.dto;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import java.util.List;

public record ShopsCache(
        List<ShopCache> shopCaches
) {
    public static ShopsCache from(List<Shop> shops) {
        return new ShopsCache(shops.stream().map(ShopCache::from).toList());
    }
}
