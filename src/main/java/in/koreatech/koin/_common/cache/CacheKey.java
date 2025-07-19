package in.koreatech.koin._common.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheKey {

    ORDERABLE_SHOP_MENUS("orderableShopMenus", 15L),
    CAMPUS_DELIVERY_ADDRESS("campusDeliveryAddress", 1440L),
    RIDER_MESSAGES("riderMessages", 1440L),
    ORDERABLE_SHOP_INFO_SUMMARY("orderableShopInfoSummary", 15L);

    public static final String ORDERABLE_SHOP_MENUS_CACHE = "orderableShopMenus";
    public static final String CAMPUS_DELIVERY_ADDRESS_CACHE = "campusDeliveryAddress";
    public static final String RIDER_MESSAGES_CACHE = "riderMessages";
    public static final String ORDERABLE_SHOP_INFO_SUMMARY_CACHE = "orderableShopInfoSummary";

    private final String cacheNames;
    private final Long ttl;

}
