package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.shop.model.menu.MenuCategory;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.menu.MenuCategoryRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class MenuCategoryFixture {

    private final MenuCategoryRepository menuCategoryRepository;

    public MenuCategoryFixture(MenuCategoryRepository menuCategoryRepository) {
        this.menuCategoryRepository = menuCategoryRepository;
    }

    public MenuCategory 추천메뉴(Shop shop) {
        return menuCategoryRepository.save(
            MenuCategory.builder()
                .shop(shop)
                .name("추천 메뉴")
                .build()
        );
    }

    public MenuCategory 사이드메뉴(Shop shop) {
        return menuCategoryRepository.save(
            MenuCategory.builder()
                .shop(shop)
                .name("사이드 메뉴")
                .build()
        );
    }

    public MenuCategory 세트메뉴(Shop shop) {
        return menuCategoryRepository.save(
            MenuCategory.builder()
                .shop(shop)
                .name("세트 메뉴")
                .build()
        );
    }

    public MenuCategory 메인메뉴(Shop shop) {
        return menuCategoryRepository.save(
            MenuCategory.builder()
                .shop(shop)
                .name("메인 메뉴")
                .build()
        );
    }
}
