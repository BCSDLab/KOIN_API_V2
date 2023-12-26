package in.koreatech.koin.domain.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.shop.domain.Menu;
import in.koreatech.koin.domain.shop.domain.MenuCategory;
import in.koreatech.koin.domain.shop.domain.MenuCategoryMap;
import in.koreatech.koin.domain.shop.dto.ShopMenuResponse;
import in.koreatech.koin.domain.shop.repository.MenuRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopService {

    private final MenuRepository menuRepository;

    public ShopMenuResponse findMenu(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다."));

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
}
