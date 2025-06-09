package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.shop.repository.shop.AdminShopParentCategoryRepository;
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationMessage;
import in.koreatech.koin.domain.shop.model.shop.ShopParentCategory;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class ShopParentCategoryFixture {

    private final AdminShopParentCategoryRepository shopParentCategoryRepository;

    public ShopParentCategoryFixture(AdminShopParentCategoryRepository shopParentCategoryRepository) {
        this.shopParentCategoryRepository = shopParentCategoryRepository;
    }

    public ShopParentCategory 상위_카테고리_가게(ShopNotificationMessage notificationMessage) {
        return shopParentCategoryRepository.save(
            ShopParentCategory.builder()
                .name("가게")
                .notificationMessage(notificationMessage)
                .build()
        );
    }

    public ShopParentCategory 상위_카테고리_콜벤(ShopNotificationMessage notificationMessage) {
        return shopParentCategoryRepository.save(
            ShopParentCategory.builder()
                .name("콜벤")
                .notificationMessage(notificationMessage)
                .build()
        );
    }
}
