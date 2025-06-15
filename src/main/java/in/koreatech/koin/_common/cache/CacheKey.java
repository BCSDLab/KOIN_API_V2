package in.koreatech.koin._common.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheKey {

    ORDERABLE_SHOP_MENUS("orderableShopMenus", 15L);

    public static final String ORDERABLE_SHOP_MENUS_CACHE = "orderableShopMenus";

    private final String cacheNames;
    private final Long ttl;

}
