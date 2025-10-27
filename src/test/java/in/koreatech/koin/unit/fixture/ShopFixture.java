package in.koreatech.koin.unit.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.shop.Shop;

public class ShopFixture {

    private ShopFixture() {}

    public static Shop 주문전환_이전_상점(Owner owner) {
        Shop shop = Shop.builder()
            .name("테스트 상점")
            .owner(owner)
            .phone("041-123-4567")
            .address("천안시 동남구")
            .description("테스트 상점입니다")
            .build();
        ReflectionTestUtils.setField(shop, "id", 100);

        return shop;
    }
}
