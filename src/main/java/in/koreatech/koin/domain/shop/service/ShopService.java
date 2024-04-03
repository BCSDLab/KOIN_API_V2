package in.koreatech.koin.domain.shop.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.shop.dto.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.ShopCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.ShopEventsResponse;
import in.koreatech.koin.domain.shop.dto.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.ShopResponse;
import in.koreatech.koin.domain.shop.dto.ShopsResponse;
import in.koreatech.koin.domain.shop.model.EventArticle;
import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.repository.EventArticleRepository;
import in.koreatech.koin.domain.shop.repository.MenuCategoryRepository;
import in.koreatech.koin.domain.shop.repository.MenuRepository;
import in.koreatech.koin.domain.shop.repository.ShopCategoryRepository;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopService {

    private final Clock clock;
    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final ShopRepository shopRepository;
    private final ShopCategoryRepository shopCategoryRepository;
    private final EventArticleRepository eventArticleRepository;

    public MenuDetailResponse findMenu(Long menuId) {
        Menu menu = menuRepository.getById(menuId);

        List<MenuCategory> menuCategories = menu.getMenuCategoryMaps()
            .stream()
            .map(MenuCategoryMap::getMenuCategory)
            .toList();

        return MenuDetailResponse.createMenuDetailResponse(menu, menuCategories);
    }

    public MenuCategoriesResponse getMenuCategories(Long shopId) {
        Shop shop = shopRepository.getById(shopId);
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByShopId(shop.getId());
        return MenuCategoriesResponse.from(menuCategories);
    }

    public ShopResponse getShop(Long shopId) {
        Shop shop = shopRepository.getById(shopId);
        Boolean eventDuration = eventArticleRepository.isEvent(shopId, LocalDate.now(clock));
        return ShopResponse.from(shop, eventDuration);
    }

    public ShopMenuResponse getShopMenu(Long shopId) {
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByShopId(shopId);
        return ShopMenuResponse.from(menuCategories);
    }

    public ShopsResponse getShops() {
        List<Shop> shops = shopRepository.findAll();
        return ShopsResponse.from(shops);
    }

    public ShopCategoriesResponse getShopsCategories() {
        List<ShopCategory> shopCategories = shopCategoryRepository.findAll();
        return ShopCategoriesResponse.from(shopCategories);
    }

    public ShopEventsResponse getEvents(Long shopId) {
        List<EventArticle> eventArticles = eventArticleRepository.findAllByShopId(shopId);
        return ShopEventsResponse.from(eventArticles);
    }
}
