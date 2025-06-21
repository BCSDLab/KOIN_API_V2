package in.koreatech.koin.unit.fixutre;

import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.shop.model.shop.Shop;

public class OrderableShopFixture {

    private OrderableShopFixture() {}

    public static OrderableShop 김밥천국() {
        Shop shop = Shop.builder()
            .name("김밥천국")
            .internalName("김천")
            .chosung("김")
            .phone("010-7574-1212")
            .address("천안시 동남구 병천면 1600")
            .description("김밥천국입니다.")
            .delivery(true)
            .deliveryPrice(3000)
            .payCard(true)
            .payBank(true)
            .isDeleted(false)
            .isEvent(false)
            .remarks("비고")
            .hit(0)
            .bank("국민")
            .accountNumber("01022595923")
            .build();

        ReflectionTestUtils.setField(shop, "id", 1);

        OrderableShop orderableShop = OrderableShop.builder()
            .shop(shop)
            .minimumOrderAmount(15000)
            .takeout(true)
            .delivery(true)
            .serviceEvent(false)
            .isDeleted(false)
            .build();

        ReflectionTestUtils.setField(orderableShop, "id", 1);

        return orderableShop;
    }
}
