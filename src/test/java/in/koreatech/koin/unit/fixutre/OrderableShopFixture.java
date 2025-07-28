package in.koreatech.koin.unit.fixutre;

import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.model.entity.shop.ShopOperation;
import in.koreatech.koin.domain.shop.model.shop.Shop;

public class OrderableShopFixture {

    private OrderableShopFixture() {}

    public static OrderableShop 김밥천국(Integer id) {
        ShopOperation shopOperation = ShopOperation.builder()
            .isOpen(true)
            .isDeleted(false)
            .build();

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
            .shopOperation(shopOperation)
            .build();

        ReflectionTestUtils.setField(shop, "id", id);
        ReflectionTestUtils.setField(shopOperation, "shop", shop);

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

    public static OrderableShop 마슬랜() {
        ShopOperation shopOperation = ShopOperation.builder()
            .isOpen(true)
            .isDeleted(false)
            .build();

        Shop shop = Shop.builder()
            .name("마슬랜")
            .internalName("마슬랜")
            .chosung("마")
            .phone("010-7574-1212")
            .address("천안시 동남구 병천면 1700")
            .description("마슬랜입니다.")
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
            .shopOperation(shopOperation)
            .build();

        ReflectionTestUtils.setField(shop, "id", 2);
        ReflectionTestUtils.setField(shopOperation, "shop", shop);

        OrderableShop orderableShop = OrderableShop.builder()
            .shop(shop)
            .minimumOrderAmount(15000)
            .takeout(true)
            .delivery(true)
            .serviceEvent(false)
            .isDeleted(false)
            .build();

        ReflectionTestUtils.setField(orderableShop, "id", 2);

        return orderableShop;
    }

    public static OrderableShop 영업시간이_아닌_김밥천국() {
        ShopOperation shopOperation = ShopOperation.builder()
            .isOpen(false)
            .isDeleted(false)
            .build();

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
            .shopOperation(shopOperation)
            .build();

        ReflectionTestUtils.setField(shop, "id", 1);
        ReflectionTestUtils.setField(shopOperation, "shop", shop);

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
