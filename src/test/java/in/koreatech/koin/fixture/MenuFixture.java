package in.koreatech.koin.fixture;

import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.MenuImage;
import in.koreatech.koin.domain.shop.model.MenuOption;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.repository.MenuCategoryMapRepository;
import in.koreatech.koin.domain.shop.repository.MenuRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class MenuFixture {

    private final MenuRepository menuRepository;
    private final MenuCategoryMapRepository menuCategoryMapRepository;

    public MenuFixture(
        MenuRepository menuRepository,
        MenuCategoryMapRepository menuCategoryMapRepository
    ) {
        this.menuRepository = menuRepository;
        this.menuCategoryMapRepository = menuCategoryMapRepository;
    }

    public Menu 짜장면_옵션메뉴(Shop shop, MenuCategory menuCategory) {
        Menu menu = Menu.builder()
            .shopId(shop.getId())
            .name("짜장면")
            .description("맛있는 짜장면")
            .build();

        menu.getMenuImages().addAll(
            List.of(
                MenuImage.builder()
                    .menu(menu)
                    .imageUrl("https://test.com/짜장면.jpg")
                    .build(),
                MenuImage.builder()
                    .menu(menu)
                    .imageUrl("https://test.com/짜장면22.jpg")
                    .build()
            )
        );
        menu.getMenuOptions().addAll(
            List.of(
                MenuOption.builder()
                    .menu(menu)
                    .option("일반")
                    .price(7000)
                    .build(),
                MenuOption.builder()
                    .menu(menu)
                    .option("곱빼기")
                    .price(7500)
                    .build()
            )
        );
        MenuCategoryMap menuCategoryMap = MenuCategoryMap.builder()
            .menu(menu)
            .menuCategory(menuCategory)
            .build();
        menu.getMenuCategoryMaps().add(menuCategoryMap);
        menuCategory.getMenuCategoryMaps().add(menuCategoryMap);
        return menuRepository.save(menu);
    }

    public Menu 짜장면_단일메뉴(Shop shop, MenuCategory menuCategory) {
        Menu menu = Menu.builder()
            .shopId(shop.getId())
            .name("짜장면")
            .description("맛있는 짜장면")
            .build();

        menu.getMenuImages().addAll(
            List.of(
                MenuImage.builder()
                    .menu(menu)
                    .imageUrl("https://test.com/짜장면.jpg")
                    .build(),
                MenuImage.builder()
                    .menu(menu)
                    .imageUrl("https://test.com/짜장면22.jpg")
                    .build()
            )
        );
        menu.getMenuOptions().add(
            MenuOption.builder()
                .menu(menu)
                .option("짜장면")
                .price(7000)
                .build()
        );
        MenuCategoryMap menuCategoryMap = MenuCategoryMap.builder()
            .menu(menu)
            .menuCategory(menuCategory)
            .build();
        menu.getMenuCategoryMaps().add(menuCategoryMap);
        menuCategory.getMenuCategoryMaps().add(menuCategoryMap);
        return menuRepository.save(menu);
    }
}
