package in.koreatech.koin.domain.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.shop.dto.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.ShopResponse;
import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.ShopImage;
import in.koreatech.koin.domain.shop.model.ShopOpen;
import in.koreatech.koin.domain.shop.repository.MenuCategoryRepository;
import in.koreatech.koin.domain.shop.repository.MenuRepository;
import in.koreatech.koin.domain.shop.repository.ShopCategoryMapRepository;
import in.koreatech.koin.domain.shop.repository.ShopImageRepository;
import in.koreatech.koin.domain.shop.repository.ShopOpenRepository;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopService {

    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final ShopRepository shopRepository;

    public MenuDetailResponse findMenu(Long menuId) {
        Menu menu = menuRepository.getById(menuId);

        List<MenuCategory> menuCategories = menu.getMenuCategoryMaps()
            .stream()
            .map(MenuCategoryMap::getMenuCategory)
            .toList();

        return createMenuDetailResponse(menu, menuCategories);
    }

    private MenuDetailResponse createMenuDetailResponse(Menu menu, List<MenuCategory> menuCategories) {
        if (menu.hasMultipleOption()) {
            return MenuDetailResponse.createForMultipleOption(menu, menuCategories);
        }
        return MenuDetailResponse.createForSingleOption(menu, menuCategories);
    }

    public MenuCategoriesResponse getMenuCategories(Long shopId) {
        Shop shop = shopRepository.getById(shopId);
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByShopId(shop.getId());
        return MenuCategoriesResponse.from(menuCategories);
    }

    public ShopResponse getShop(Long shopId) {
        Shop shop = shopRepository.getById(shopId);
        return ShopResponse.from(shop);
    }

    public ShopMenuResponse getShopMenu(Long shopId) {
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByShopId(shopId);
        return ShopMenuResponse.from(menuCategories);
    }
}
