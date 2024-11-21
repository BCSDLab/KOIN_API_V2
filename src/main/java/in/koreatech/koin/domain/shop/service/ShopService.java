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
import in.koreatech.koin.domain.shop.model.redis.ShopReviewNotification;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.repository.shop.ShopCategoryRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopReviewNotificationRedisRepository;
import in.koreatech.koin.domain.shop.repository.shop.dto.ShopCustomRepository;
import in.koreatech.koin.domain.shop.repository.shop.dto.ShopInfo;
import in.koreatech.koin.global.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    private final ShopsCacheService shopsCache;
    private final ShopCustomRepository shopCustomRepository;
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final ShopReviewNotificationRedisRepository shopReviewNotificationRedisRepository;

    public ShopResponse getShop(Integer shopId) {
        Shop shop = shopRepository.getById(shopId);
        LocalDate now = LocalDate.now(clock);
        return ShopResponse.from(shop, now);
    }

    public ShopsResponse getShops() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Shop> shops = shopRepository.findAll();
        // TODO: Integer에 shopId?
        Map<Integer, Boolean> eventDuration = shopRepository.getAllShopEventDuration(now.toLocalDate());
        return ShopsResponse.from(shops, eventDuration, now);
    }

    public ShopCategoriesResponse getShopsCategories() {
        List<ShopCategory> shopCategories = shopCategoryRepository.findAll(Sort.by("orderIndex"));
        return ShopCategoriesResponse.from(shopCategories);
    }

    // TODO: query가 뭐지?
    public ShopsResponseV2 getShopsV2(
        ShopsSortCriteria sortBy,
        List<ShopsFilterCriteria> filterCriteria,
        String query
    ) {
        if (filterCriteria.contains(null)) {
            throw KoinIllegalArgumentException.withDetail("유효하지 않은 필터입니다.");
        }
        ShopsCache shopCaches = shopsCache.findAllShopCache();
        LocalDateTime now = LocalDateTime.now(clock);
        Map<Integer, ShopInfo> shopInfoMap = shopCustomRepository.findAllShopInfo(now);
        return ShopsResponseV2.from(
            shopCaches.shopCaches(),
            shopInfoMap,
            sortBy,
            filterCriteria,
            now,
            query
        );
    }

    // TODO: 한시간 뒤에 어디서 쏘나요?
    public void publishCallNotification(Integer shopId, Integer studentId) {
        if (!isSubscribeReviewNotification(studentId)) {
            return;
        }
        shopRepository.getById(shopId); // TODO: 메서드 이름 개선 ex) validateShopId
        ShopReviewNotification shopReviewNotification = ShopReviewNotification.builder()
            .shopId(shopId)
            .studentId(studentId)
            .build();
        // TODO: 갑자기 score가 나옴 save에 숨기자
        double score = LocalDateTime.now(clock).plusHours(1).toEpochSecond(ZoneOffset.UTC);
        // TODO: score대신 now를 인자로 받자
        shopReviewNotificationRedisRepository.save(shopReviewNotification, score);
    }

    private boolean isSubscribeReviewNotification(Integer studentId) {
        return notificationSubscribeRepository.existsByUserIdAndSubscribeType(studentId, REVIEW_PROMPT);
    }
}
