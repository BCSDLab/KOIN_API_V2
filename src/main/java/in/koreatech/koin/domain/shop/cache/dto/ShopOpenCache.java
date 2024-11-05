package in.koreatech.koin.domain.shop.cache.dto;

import in.koreatech.koin.domain.shop.model.shop.ShopOpen;
import java.time.LocalTime;

public record ShopOpenCache(
        String dayOfWeek,
        boolean closed,
        LocalTime openTime,
        LocalTime closeTime
) {
    public static ShopOpenCache from(ShopOpen shopOpen) {
        return new ShopOpenCache(
                shopOpen.getDayOfWeek(),
                shopOpen.isClosed(),
                shopOpen.getOpenTime(),
                shopOpen.getCloseTime()
        );
    }
}
