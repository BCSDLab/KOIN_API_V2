package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.repository.ShopCategoryRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class ShopCategoryFixture {

    private final ShopCategoryRepository categoryRepository;

    public ShopCategoryFixture(ShopCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public ShopCategory 카테고리_치킨() {
        return categoryRepository.save(
            ShopCategory.builder()
                .isDeleted(false)
                .name("치킨")
                .imageUrl("https://test-image.com/ckicken.jpg")
                .build()
        );
    }

    public ShopCategory 카테고리_일반음식() {
        return categoryRepository.save(
            ShopCategory.builder()
                .isDeleted(false)
                .name("일반음식점")
                .imageUrl("https://test-image.com/normal.jpg")
                .build()
        );
    }
}
