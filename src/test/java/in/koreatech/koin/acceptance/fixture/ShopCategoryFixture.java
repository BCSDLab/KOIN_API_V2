package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.model.shop.ShopParentCategory;
import in.koreatech.koin.domain.shop.repository.shop.ShopCategoryRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class ShopCategoryFixture {

    private final ShopCategoryRepository categoryRepository;

    public ShopCategoryFixture(ShopCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public ShopCategory 카테고리_치킨(ShopParentCategory parentCategory) {
        return categoryRepository.save(
            ShopCategory.builder()
                .name("치킨")
                .imageUrl("https://test-image.com/ckicken.jpg")
                .orderIndex(2)
                .parentCategory(parentCategory)
                .eventBannerImageUrl("https://test-image.com/chicken-event.jpg")
                .build()
        );
    }

    public ShopCategory 카테고리_일반음식(ShopParentCategory parentCategory) {
        return categoryRepository.save(
            ShopCategory.builder()
                .name("일반음식점")
                .imageUrl("https://test-image.com/normal.jpg")
                .orderIndex(1)
                .parentCategory(parentCategory)
                .eventBannerImageUrl("https://test-image.com/normal-event.jpg")
                .build()
        );
    }
}
