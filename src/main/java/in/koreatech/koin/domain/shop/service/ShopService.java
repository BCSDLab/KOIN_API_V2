package in.koreatech.koin.domain.shop.service;

import static in.koreatech.koin.domain.shop.dto.shop.ShopsResponse.InnerShopResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.shop.dto.menu.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.menu.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.shop.ShopCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.shop.ShopEventsResponse;
import in.koreatech.koin.domain.shop.dto.menu.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.shop.ShopResponse;
import in.koreatech.koin.domain.shop.dto.shop.ShopsFilterCriteria;
import in.koreatech.koin.domain.shop.dto.shop.ShopsResponse;
import in.koreatech.koin.domain.shop.dto.shop.ShopsResponseV2;
import in.koreatech.koin.domain.shop.dto.shop.ShopsSortCriteria;
import in.koreatech.koin.domain.shop.model.menu.Menu;
import in.koreatech.koin.domain.shop.model.menu.MenuCategory;
import in.koreatech.koin.domain.shop.model.menu.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.repository.EventArticleRepository;
import in.koreatech.koin.domain.shop.repository.MenuCategoryRepository;
import in.koreatech.koin.domain.shop.repository.MenuRepository;
import in.koreatech.koin.domain.shop.repository.ShopCategoryRepository;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.domain.shop.repository.redis.ShopsRedisRepository;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
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
    private final ShopsRedisRepository shopsRedisRepository;

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
        if (!shopsRedisRepository.isCacheAvailable()) {
            refreshShopsCache();
        }
        return shopsRedisRepository.getShopsResponseByRedis();
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

    public void refreshShopsCache() {
        List<Shop> shops = shopRepository.findAll();
        LocalDateTime now = LocalDateTime.now(clock);
        List<InnerShopResponse> innerShopResponses = shops.stream().map(shop -> {
                boolean isDurationEvent = eventArticleRepository.isDurationEvent(shop.getId(), now.toLocalDate());
                return InnerShopResponse.from(shop, isDurationEvent, shop.isOpen(now));
            })
            .sorted(Comparator.comparing(InnerShopResponse::isOpen, Comparator.reverseOrder())).toList();
        ShopsResponse shopsResponse = ShopsResponse.from(innerShopResponses);
        shopsRedisRepository.save(shopsResponse);
    }

    public ShopsResponseV2 getShopsV2(ShopsSortCriteria sortBy, List<ShopsFilterCriteria> shopsFilterCriterias) {
        if (shopsFilterCriterias.contains(null)) {
            throw KoinIllegalArgumentException.withDetail("유효하지 않은 필터입니다.");
        }
        List<Shop> shops = shopRepository.findAll();
        LocalDateTime now = LocalDateTime.now(clock);
        List<ShopsResponseV2.InnerShopResponse> innerShopResponses = shops.stream()
            .filter(ShopsFilterCriteria.createCombinedFilter(shopsFilterCriterias, now))
            .map(shop -> {
                boolean isDurationEvent = eventArticleRepository.isDurationEvent(shop.getId(), now.toLocalDate());
                return ShopsResponseV2.InnerShopResponse.from(shop, isDurationEvent, shop.isOpen(now));
            })
            .sorted(ShopsResponseV2.InnerShopResponse.getComparator(sortBy))
            .toList();
        ShopsResponseV2 shopsResponse = ShopsResponseV2.from(innerShopResponses);
        return shopsResponse;
    }

}
