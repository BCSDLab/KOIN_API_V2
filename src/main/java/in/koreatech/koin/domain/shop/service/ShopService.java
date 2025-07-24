package in.koreatech.koin.domain.shop.service;

import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.REVIEW_PROMPT;

import in.koreatech.koin.domain.benefit.model.BenefitCategoryMap;
import in.koreatech.koin.domain.benefit.repository.BenefitCategoryMapRepository;
import in.koreatech.koin.domain.shop.cache.ShopsCacheService;
import in.koreatech.koin.domain.shop.cache.dto.ShopsCache;
import in.koreatech.koin.domain.shop.dto.shop.ShopsFilterCriteria;
import in.koreatech.koin.domain.shop.dto.shop.ShopsFilterCriteriaV3;
import in.koreatech.koin.domain.shop.dto.shop.ShopsSortCriteria;
import in.koreatech.koin.domain.shop.dto.shop.ShopsSortCriteriaV3;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopResponse;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopsResponse;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopsResponseV2;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopsResponseV3;
import in.koreatech.koin.domain.shop.model.redis.ShopReviewNotification;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.repository.shop.ShopCategoryRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopReviewNotificationRedisRepository;
import in.koreatech.koin.domain.shop.repository.shop.dto.ShopCustomRepository;
import in.koreatech.koin.domain.shop.repository.shop.dto.ShopInfo;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final BenefitCategoryMapRepository benefitCategoryMapRepository;

    public ShopResponse getShop(Integer shopId) {
        Shop shop = shopRepository.getById(shopId);
        LocalDate now = LocalDate.now(clock);
        return ShopResponse.from(shop, now);
    }

    public ShopsResponse getShops() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Shop> shops = shopRepository.findAll();
        Map<Integer, Boolean> eventDuration = shopRepository.getAllShopEventDuration(now.toLocalDate());
        return ShopsResponse.from(shops, eventDuration, now);
    }

    public ShopCategoriesResponse getShopsCategories() {
        List<ShopCategory> shopCategories = shopCategoryRepository.findAll(Sort.by("orderIndex"));
        return ShopCategoriesResponse.from(shopCategories);
    }

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
        List<BenefitCategoryMap> benefitCategorys = benefitCategoryMapRepository.findAllWithFetchJoin();
        Map<Integer, List<String>> benefitDetailMap = new HashMap<>(benefitCategorys.size());
        benefitCategorys.forEach(benefitCategory -> {
            int shopId = benefitCategory.getShop().getId();
            String benefitDetail = benefitCategory.getDetail();
            if (benefitDetailMap.containsKey(shopId)) {
                benefitDetailMap.get(shopId).add(benefitDetail);
            } else {
                List<String> details = new ArrayList<>();
                details.add(benefitDetail);
                benefitDetailMap.put(shopId, details);
            }
        });
        return ShopsResponseV2.from(
                shopCaches.shopCaches(),
                shopInfoMap,
                sortBy,
                filterCriteria,
                now,
                query,
                benefitDetailMap
        );
    }

    public ShopsResponseV3 getShopsV3(
        ShopsSortCriteriaV3 sortBy,
        List<ShopsFilterCriteriaV3> filterCriteria,
        String query
    ) {
        if (filterCriteria.contains(null)) {
            throw KoinIllegalArgumentException.withDetail("유효하지 않은 필터입니다.");
        }
        ShopsCache shopCaches = shopsCache.findAllShopCache();
        LocalDateTime now = LocalDateTime.now(clock);
        Map<Integer, ShopInfo> shopInfoMap = shopCustomRepository.findAllShopInfo(now);
        Map<Integer, List<String>> shopImageMap = shopCustomRepository.findAllShopImage();
        List<Integer> orderableShopIds = shopCustomRepository.findAllOrderableShopId();
        List<BenefitCategoryMap> benefitCategorys = benefitCategoryMapRepository.findAllWithFetchJoin();
        Map<Integer, List<String>> benefitDetailMap = new HashMap<>(benefitCategorys.size());
        benefitCategorys.forEach(benefitCategory -> {
            int shopId = benefitCategory.getShop().getId();
            String benefitDetail = benefitCategory.getDetail();
            if (benefitDetailMap.containsKey(shopId)) {
                benefitDetailMap.get(shopId).add(benefitDetail);
            } else {
                List<String> details = new ArrayList<>();
                details.add(benefitDetail);
                benefitDetailMap.put(shopId, details);
            }
        });
        return ShopsResponseV3.from(
            shopCaches.shopCaches(),
            shopInfoMap,
            sortBy,
            filterCriteria,
            now,
            query,
            benefitDetailMap,
            shopImageMap,
            orderableShopIds
        );
    }

    public void publishCallNotification(Integer shopId, Integer studentId) {
        shopRepository.getById(shopId);

        if (isSubscribeReviewNotification(studentId)) {
            ShopReviewNotification shopReviewNotification = ShopReviewNotification.builder()
                    .shopId(shopId)
                    .studentId(studentId)
                    .build();

            double score = LocalDateTime.now(clock).plusHours(1).toEpochSecond(ZoneOffset.UTC);
            shopReviewNotificationRedisRepository.save(shopReviewNotification, score);
        }
    }

    private boolean isSubscribeReviewNotification(Integer studentId) {
        return notificationSubscribeRepository.existsByUserIdAndSubscribeTypeAndDetailTypeIsNull(studentId, REVIEW_PROMPT);
    }
}
