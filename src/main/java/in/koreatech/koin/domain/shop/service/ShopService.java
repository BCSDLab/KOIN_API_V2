package in.koreatech.koin.domain.shop.service;

import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.REVIEW_PROMPT;

import in.koreatech.koin.domain.shop.cache.ShopsCacheService;
import in.koreatech.koin.domain.shop.cache.dto.ShopsCache;
import in.koreatech.koin.domain.shop.dto.menu.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.menu.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.menu.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.shop.ShopCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.shop.ShopEventsWithBannerUrlResponse;
import in.koreatech.koin.domain.shop.dto.shop.ShopEventsWithThumbnailUrlResponse;
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
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationBuffer;
import in.koreatech.koin.domain.shop.repository.event.EventArticleRepository;
import in.koreatech.koin.domain.shop.repository.menu.MenuCategoryRepository;
import in.koreatech.koin.domain.shop.repository.menu.MenuRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopCategoryRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopNotificationBufferRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.shop.repository.shop.dto.ShopCustomRepository;
import in.koreatech.koin.domain.shop.repository.shop.dto.ShopInfoV1;
import in.koreatech.koin.domain.shop.repository.shop.dto.ShopInfoV2;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ShopsCacheService shopsCache;
    private final ShopCustomRepository shopCustomRepository;
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final ShopNotificationBufferRepository shopNotificationBufferRepository;
    private final UserRepository userRepository;

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
        LocalDateTime now = LocalDateTime.now(clock);
        List<Shop> shops = shopRepository.findAll();
        Map<Integer, ShopInfoV1> shopEventMap = shopCustomRepository.findAllShopEvent(now);
        return ShopsResponse.from(shops, shopEventMap, now);
    }

    public ShopCategoriesResponse getShopsCategories() {
        List<ShopCategory> shopCategories = shopCategoryRepository.findAll();
        return ShopCategoriesResponse.from(shopCategories);
    }

    public ShopEventsWithThumbnailUrlResponse getShopEvents(Integer shopId) {
        Shop shop = shopRepository.getById(shopId);
        return ShopEventsWithThumbnailUrlResponse.of(shop, clock);
    }

    public ShopEventsWithBannerUrlResponse getAllEvents() {
        List<Shop> shops = shopRepository.findAll();
        return ShopEventsWithBannerUrlResponse.of(shops, clock);
    }

    public ShopsResponseV2 getShopsV2(
            ShopsSortCriteria sortBy,
            List<ShopsFilterCriteria> shopsFilterCriterias,
            String query
    ) {
        if (shopsFilterCriterias.contains(null)) {
            throw KoinIllegalArgumentException.withDetail("유효하지 않은 필터입니다.");
        }
        ShopsCache shopCaches = shopsCache.findAllShopCache();
        LocalDateTime now = LocalDateTime.now(clock);
        Map<Integer, ShopInfoV2> shopInfoMap = shopCustomRepository.findAllShopInfo(now);
        return ShopsResponseV2.from(
                shopCaches.shopCaches(),
                shopInfoMap,
                sortBy,
                shopsFilterCriterias,
                now,
                query
        );
    }

    @Transactional
    public void publishCallNotification(Integer shopId, Integer studentId) {
        shopRepository.getById(shopId);

        if (isSubscribeReviewNotification(studentId)) {
            Shop shop = shopRepository.getById(shopId);
            User user = userRepository.getById(studentId);

            ShopNotificationBuffer shopNotificationBuffer = ShopNotificationBuffer.builder()
                .shop(shop)
                .user(user)
                .notificationTime(LocalDateTime.now().plusHours(1))
                .build();

            shopNotificationBufferRepository.save(shopNotificationBuffer);
        }
    }

    private boolean isSubscribeReviewNotification(Integer studentId) {
        return notificationSubscribeRepository
            .existsByUserIdAndSubscribeType(studentId, REVIEW_PROMPT);
    }
}
