package in.koreatech.koin.domain.shop.service;

import static in.koreatech.koin.domain.shop.dto.ShopsResponse.InnerShopResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
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

    public MenuDetailResponse findMenu(Integer menuId) {
        Menu menu = menuRepository.getById(menuId);

        List<MenuCategory> menuCategories = menu.getMenuCategoryMaps()
            .stream()
            .map(MenuCategoryMap::getMenuCategory)
            .toList();

        return MenuDetailResponse.createMenuDetailResponse(menu, menuCategories);
    }

    public MenuCategoriesResponse getMenuCategories(Integer shopId) {
        Shop shop = shopRepository.getById(shopId);
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByShopId(shop.getId());
        Collections.sort(menuCategories);
        return MenuCategoriesResponse.from(menuCategories);
    }

    public ShopResponse getShop(Integer shopId) {
        Shop shop = shopRepository.getById(shopId);
        boolean eventDuration = eventArticleRepository.isDurationEvent(shopId, LocalDate.now(clock));
        return ShopResponse.from(shop, eventDuration);
    }

    public ShopMenuResponse getShopMenus(Integer shopId) {
        Shop shop = shopRepository.getById(shopId);
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByShopId(shop.getId());
        Collections.sort(menuCategories);
        return ShopMenuResponse.from(menuCategories);
    }

    public ShopsResponse getShops() {
        List<Shop> shops = shopRepository.findAll();
        LocalDateTime now = LocalDateTime.now(clock);
        List<InnerShopResponse> innerShopResponses = shops.stream().map(shop -> {
                boolean isDurationEvent = eventArticleRepository.isDurationEvent(shop.getId(), now.toLocalDate());
                return InnerShopResponse.from(shop, isDurationEvent, shop.isOpen(now));
            })
            .sorted(Comparator.comparing(InnerShopResponse::isOpen, Collections.reverseOrder())).toList();
        return ShopsResponse.from(innerShopResponses);
    }

    public ShopCategoriesResponse getShopsCategories() {
        List<ShopCategory> shopCategories = shopCategoryRepository.findAll();
        return ShopCategoriesResponse.from(shopCategories);
    }

    public ShopEventsResponse getShopEvents(Integer shopId) {
        Shop shop = shopRepository.getById(shopId);
        return ShopEventsResponse.of(shop, clock);
    }

    public ShopEventsResponse getAllEvents() {
        List<Shop> shops = shopRepository.findAll();
        return ShopEventsResponse.of(shops, clock);
    }
}
