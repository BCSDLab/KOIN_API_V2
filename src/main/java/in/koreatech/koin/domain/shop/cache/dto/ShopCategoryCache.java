package in.koreatech.koin.domain.shop.cache.dto;

import in.koreatech.koin.domain.shop.model.shop.ShopCategoryMap;

public record ShopCategoryCache(
        Integer id,
        String name,
        String imageUrl
) {

    public static ShopCategoryCache from(ShopCategoryMap shopCategoryMap) {
        return new ShopCategoryCache(
                shopCategoryMap.getShopCategory().getId(),
                shopCategoryMap.getShopCategory().getName(),
                shopCategoryMap.getShopCategory().getImageUrl()
        );
    }
}
