package in.koreatech.koin.domain.shop.service;

import static in.koreatech.koin.domain.shop.dto.ShopsResponse.InnerShopResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.shop.dto.CreateReviewRequest;
import in.koreatech.koin.domain.shop.dto.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.ModifyReviewRequest;
import in.koreatech.koin.domain.shop.dto.ShopReviewResponse;
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
import in.koreatech.koin.domain.shop.model.ShopReview;
import in.koreatech.koin.domain.shop.model.ShopReviewImage;
import in.koreatech.koin.domain.shop.model.ShopReviewMenu;
import in.koreatech.koin.domain.shop.repository.EventArticleRepository;
import in.koreatech.koin.domain.shop.repository.MenuCategoryRepository;
import in.koreatech.koin.domain.shop.repository.MenuRepository;
import in.koreatech.koin.domain.shop.repository.ReviewImageRepository;
import in.koreatech.koin.domain.shop.repository.ShopCategoryRepository;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewImageRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewMenuRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewRepository;
import in.koreatech.koin.domain.shop.repository.redis.ShopsRedisRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import jakarta.persistence.EntityManager;
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
    private final ShopReviewRepository shopReviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final UserRepository userRepository;
    private final ShopReviewImageRepository shopReviewImageRepository;
    private final ShopReviewMenuRepository shopReviewMenuRepository;

    private final EntityManager entityManager;

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

    public ShopReviewResponse getReviewsByShopId(Integer shopId) {
        List<ShopReview> reviews = shopReviewRepository.findAllByShopId(shopId);
        return ShopReviewResponse.from(reviews);
    }

    @Transactional
    public void createReview(CreateReviewRequest createReviewRequest, Integer userId, Integer shopId) {
        User user = userRepository.getById(userId);
        Shop shop = shopRepository.getById(shopId);
        ShopReview shopReview = ShopReview.builder()
            .reviewer(user)
            .content(createReviewRequest.content())
            .rating(createReviewRequest.rating())
            .shop(shop)
            .build();
        ShopReview savedShopReview = shopReviewRepository.save(shopReview);
        for (String imageUrl: createReviewRequest.imageUrls()) {
            shopReviewImageRepository.save(ShopReviewImage.builder()
                .review(savedShopReview)
                .imageUrls(imageUrl)
                .build());
        }
        for (String menuName: createReviewRequest.menuNames()) {
            shopReviewMenuRepository.save(ShopReviewMenu.builder()
                .review(savedShopReview)
                .menuName(menuName)
                .build());
        }
    }

    public void deleteReview(Integer shopId, Integer userId) {
        ShopReview shopReview = shopReviewRepository.getByShopIdAndReviewerId(shopId, userId);
        shopReviewRepository.deleteById(shopReview.getId());
    }

    @Transactional
    public void modifyShop(ModifyReviewRequest modifyReviewRequest, Integer reviewId, Integer userId) {
        ShopReview shopReview = shopReviewRepository.getById(reviewId);
        if (!Objects.equals(shopReview.getReviewer().getId(), userId)) {
            throw AuthenticationException.withDetail("해당 유저가 작성한 리뷰가 아닙니다.");
        }
        shopReview.modifyReview(
            modifyReviewRequest.content(),
            modifyReviewRequest.rating()
        );
        shopReview.modifyReviewImage(modifyReviewRequest.imageUrls(), entityManager);
        shopReview.modifyMenuName(modifyReviewRequest.menuNames(), entityManager);
    }
}
