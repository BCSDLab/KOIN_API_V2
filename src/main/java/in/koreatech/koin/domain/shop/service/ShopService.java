package in.koreatech.koin.domain.shop.service;

import static in.koreatech.koin.domain.shop.dto.ShopsResponse.InnerShopResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.shop.dto.CreateReviewRequest;
import in.koreatech.koin.domain.shop.dto.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.ModifyReviewRequest;
import in.koreatech.koin.domain.shop.dto.ShopCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.ShopEventsResponse;
import in.koreatech.koin.domain.shop.dto.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.ShopResponse;
import in.koreatech.koin.domain.shop.dto.ShopReviewReportCategoryResponse;
import in.koreatech.koin.domain.shop.dto.ShopReviewReportRequest;
import in.koreatech.koin.domain.shop.dto.ShopReviewResponse;
import in.koreatech.koin.domain.shop.dto.ShopsResponse;
import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.model.ShopReview;
import in.koreatech.koin.domain.shop.model.ShopReviewImage;
import in.koreatech.koin.domain.shop.model.ShopReviewMenu;
import in.koreatech.koin.domain.shop.model.ShopReviewReport;
import in.koreatech.koin.domain.shop.model.ShopReviewReportCategory;
import in.koreatech.koin.domain.shop.repository.EventArticleRepository;
import in.koreatech.koin.domain.shop.repository.MenuCategoryRepository;
import in.koreatech.koin.domain.shop.repository.MenuRepository;
import in.koreatech.koin.domain.shop.repository.ReviewImageRepository;
import in.koreatech.koin.domain.shop.repository.ShopCategoryRepository;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewImageRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewMenuRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewReportCategoryRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewReportRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewRepository;
import in.koreatech.koin.domain.shop.repository.redis.ShopsRedisRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.model.Criteria;
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

    private final JwtProvider jwtProvider;
    private final EntityManager entityManager;
    private final ShopReviewReportRepository shopReviewReportRepository;
    private final ShopReviewReportCategoryRepository shopReviewReportCategoryRepository;

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

    public ShopReviewResponse getReviewsByShopId(Integer shopId, String token, Integer page, Integer limit) {
        Integer userId = null;
        if (token != null) {
            userId = jwtProvider.getUserId(token.replaceAll("Bearer ", ""));
        }
        Integer total = shopReviewRepository.countByShopIdExcludingReportedByUser(shopId, userId);

        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(
            criteria.getPage(),
            criteria.getLimit()
        );
        Page<ShopReview> result = shopReviewRepository.findAllByShopIdExcludingReportedByUser(shopId, userId,
            pageRequest);
        return ShopReviewResponse.from(result, userId, criteria);
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
        for (String imageUrl : createReviewRequest.imageUrls()) {
            shopReviewImageRepository.save(ShopReviewImage.builder()
                .review(savedShopReview)
                .imageUrls(imageUrl)
                .build());
        }
        for (String menuName : createReviewRequest.menuNames()) {
            shopReviewMenuRepository.save(ShopReviewMenu.builder()
                .review(savedShopReview)
                .menuName(menuName)
                .build());
        }
    }

    /*
    TODO:
        - 응답 구조 이름 변경 (o)
            - rating으로 (o)
        - url에 shopId추가 (o)
        - 리뷰 조회시 헤더에 토큰이 있는 경우 (o)
            - 자신이 신고한 리뷰는 제외하고 조회되도록 한다. (o)
            - isMe = true or false (o)
        - 리뷰 조회시 헤더에 토큰이 없는 경우 (o)
            - isMe = false Only (o)
        - 리뷰 조회 페이지네이션
        - 리뷰 신고 기능 만들기
            - 리뷰 신고 API작성 (o)
            - 어드민 페이지에서 신고된 리뷰를 삭제할 수 있어야함.
                - 신고된 리뷰 조회 api작성(어드민 권한)
                - 신고된 리뷰 삭제 api작성(어드민 권한)
     */
    @Transactional
    public void deleteReview(Integer reviewId, Integer userId) {
        ShopReview shopReview = shopReviewRepository.getById(reviewId);
        if (!Objects.equals(shopReview.getReviewer().getId(), userId)) {
            throw AuthenticationException.withDetail("해당 유저가 작성한 리뷰가 아닙니다.");
        }
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

    @Transactional
    public void reportReview(
        Integer shopId,
        Integer reviewId,
        Integer userId,
        ShopReviewReportRequest shopReviewReportRequest
    ) {
        User user = userRepository.getById(userId);
        ShopReview shopReview = shopReviewRepository.getAllByIdAndShopId(reviewId, shopId);
        shopReviewReportRepository.save(
            ShopReviewReport.builder()
                .reportedBy(user)
                .review(shopReview)
                .reasonTitle(shopReviewReportRequest.title())
                .reasonDetail(shopReviewReportRequest.content())
                .build()
        );
    }

    public ShopReviewReportCategoryResponse getReviewReportCategories() {
        List<ShopReviewReportCategory> shopReviewReportCategories = shopReviewReportCategoryRepository.findAll();
        return ShopReviewReportCategoryResponse.from(shopReviewReportCategories);
    }
}
