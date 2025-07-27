package in.koreatech.koin.unit.fixutre;

import java.time.LocalTime;

import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopOpenInfo;

public class OrderableShopOpenInfoFixture {

    private OrderableShopOpenInfoFixture() {}

    public static OrderableShopOpenInfo 영업_정보(
        Integer shopId,
        String dayOfWeek,
        Boolean closed,
        LocalTime openTime,
        LocalTime closeTime
    ) {
        return new OrderableShopOpenInfo(shopId, dayOfWeek, closed, openTime, closeTime);
    }

}
