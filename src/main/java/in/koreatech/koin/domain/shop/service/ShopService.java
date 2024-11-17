package in.koreatech.koin.domain.shop.service;

import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.REVIEW_PROMPT;

import in.koreatech.koin.domain.shop.cache.ShopsCacheService;
import in.koreatech.koin.domain.shop.cache.dto.ShopsCache;
import in.koreatech.koin.domain.shop.dto.shop.ShopsFilterCriteria;
import in.koreatech.koin.domain.shop.dto.shop.ShopsSortCriteria;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopResponse;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopsResponse;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopsResponseV2;
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
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopService {

    private final Clock clock;
    private final ShopRepository shopRepository;
    private final ShopCategoryRepository shopCategoryRepository;
    private final EventArticleRepository eventArticleRepository;
    private final ShopsCacheService shopsCache;
    private final ShopCustomRepository shopCustomRepository;
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final ShopNotificationBufferRepository shopNotificationBufferRepository;
    private final UserRepository userRepository;

    public ShopResponse getShop(Integer shopId) {
        Shop shop = shopRepository.getById(shopId);
        boolean eventDuration = eventArticleRepository.isDurationEvent(shopId, LocalDate.now(clock));
        return ShopResponse.from(shop, eventDuration);
    }

    public ShopsResponse getShops() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Shop> shops = shopRepository.findAll();
        Map<Integer, ShopInfoV1> shopEventMap = shopCustomRepository.findAllShopEvent(now);
        return ShopsResponse.from(shops, shopEventMap, now);
    }

    public ShopCategoriesResponse getShopsCategories() {
        List<ShopCategory> shopCategories = shopCategoryRepository.findAll(Sort.by("orderIndex"));
        return ShopCategoriesResponse.from(shopCategories);
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
