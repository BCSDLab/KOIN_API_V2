package in.koreatech.koin._common.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheKey {

    ORDERABLE_SHOP_MENUS("orderableShopMenus", 15L),
    CAMPUS_DELIVERY_ADDRESS("campusDeliveryAddress", 1440L);

    public static final String ORDERABLE_SHOP_MENUS_CACHE = "orderableShopMenus";
    public static final String CAMPUS_DELIVERY_ADDRESS_CACHE = "campusDeliveryAddress";

    private final String cacheNames;
    private final Long ttl;

}
