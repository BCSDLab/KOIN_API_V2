package in.koreatech.koin.domain.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.shop.dto.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.ShopMenuResponse;
import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.MenuCategoryMap;
import in.koreatech.koin.domain.shop.repository.MenuCategoryRepository;
import in.koreatech.koin.domain.shop.repository.MenuRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopService {

    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;

    public ShopMenuResponse findMenu(Long menuId) {
        Menu menu = menuRepository.getById(menuId);

        List<MenuCategory> menuCategories = menu.getMenuCategoryMaps()
            .stream()
            .map(MenuCategoryMap::getMenuCategory)
            .toList();

        return createShopMenuResponse(menu, menuCategories);
    }

    private ShopMenuResponse createShopMenuResponse(Menu menu, List<MenuCategory> menuCategories) {
        if (menu.hasMultipleOption()) {
            return ShopMenuResponse.createForMultipleOption(menu, menuCategories);
        }
        return ShopMenuResponse.createForSingleOption(menu, menuCategories);
    }

    public MenuCategoriesResponse getMenuCategories(Long shopId) {
        //TODO 존재하는 상점인지 검증하고, 없다면 401를 반환하여야 한다. 작업시점: 상점 도메인 조회 기능 추가시
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByShopId(shopId);
        return MenuCategoriesResponse.from(menuCategories);
    }
}
