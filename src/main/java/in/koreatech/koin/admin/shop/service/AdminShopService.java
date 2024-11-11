package in.koreatech.koin.admin.shop.service;

import static in.koreatech.koin.domain.shop.model.review.ReportStatus.DELETED;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import in.koreatech.koin.admin.shop.dto.*;
import in.koreatech.koin.admin.shop.exception.ShopCategoryNotEmptyException;
import in.koreatech.koin.admin.shop.repository.*;
import in.koreatech.koin.domain.shop.exception.ReviewNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.shop.dto.AdminCreateShopRequest.InnerShopOpen;
import in.koreatech.koin.admin.shop.dto.AdminModifyMenuRequest.InnerOptionPrice;
import in.koreatech.koin.admin.shop.exception.ShopCategoryDuplicationException;
import in.koreatech.koin.domain.shop.model.menu.Menu;
import in.koreatech.koin.domain.shop.model.menu.MenuCategory;
import in.koreatech.koin.domain.shop.model.menu.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.menu.MenuImage;
import in.koreatech.koin.domain.shop.model.menu.MenuOption;
import in.koreatech.koin.domain.shop.model.review.ReportStatus;
import in.koreatech.koin.domain.shop.model.review.ShopReview;
import in.koreatech.koin.domain.shop.model.review.ShopReviewReport;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.model.shop.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.shop.ShopImage;
import in.koreatech.koin.domain.shop.model.shop.ShopOpen;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import in.koreatech.koin.global.model.Criteria;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminShopService {

    private final Clock clock;
    private final EntityManager entityManager;
    private final AdminEventArticleRepository adminEventArticleRepository;
    private final AdminMenuCategoryRepository adminMenuCategoryRepository;
    private final AdminShopCategoryMapRepository adminShopCategoryMapRepository;
    private final AdminShopCategoryRepository adminShopCategoryRepository;
    private final AdminShopImageRepository adminShopImageRepository;
    private final AdminShopOpenRepository adminShopOpenRepository;
    private final AdminShopRepository adminShopRepository;
    private final AdminMenuRepository adminMenuRepository;
    private final AdminMenuCategoryMapRepository adminMenuCategoryMapRepository;
    private final AdminMenuImageRepository adminMenuImageRepository;
    private final AdminMenuDetailRepository adminMenuDetailRepository;
    private final AdminShopReviewRepository adminShopReviewRepository;
    private final AdminShopReviewCustomRepository adminShopReviewCustomRepository;

    public AdminShopsResponse getShops(Integer page, Integer limit, Boolean isDeleted) {
        Integer total = adminShopRepository.countAllByIsDeleted(isDeleted);
        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));
        Page<Shop> result = adminShopRepository.findAllByIsDeleted(isDeleted, pageRequest);
        return AdminShopsResponse.of(result, criteria);
    }

    public AdminShopResponse getShop(Integer shopId) {
        Shop shop = adminShopRepository.getById(shopId);
        boolean eventDuration = adminEventArticleRepository.isDurationEvent(shopId, LocalDate.now(clock));
        return AdminShopResponse.from(shop, eventDuration);
    }

    public List<AdminShopCategoryResponse> getShopCategories() {
        List<ShopCategory> shopCategories = adminShopCategoryRepository.findAll(Sort.by("orderIndex"));
        return shopCategories.stream()
            .map(AdminShopCategoryResponse::from)
            .toList();
    }

    public AdminShopCategoryResponse getShopCategory(Integer categoryId) {
        ShopCategory shopCategory = adminShopCategoryRepository.getById(categoryId);
        return AdminShopCategoryResponse.from(shopCategory);
    }

    public AdminShopMenuResponse getAllMenus(Integer shopId) {
        Shop shop = adminShopRepository.getById(shopId);
        List<MenuCategory> menuCategories = adminMenuCategoryRepository.findAllByShopId(shop.getId());
        Collections.sort(menuCategories);
        return AdminShopMenuResponse.from(menuCategories);
    }

    public AdminMenuCategoriesResponse getAllMenuCategories(Integer shopId) {
        Shop shop = adminShopRepository.getById(shopId);
        List<MenuCategory> menuCategories = adminMenuCategoryRepository.findAllByShopId(shop.getId());
        Collections.sort(menuCategories);
        return AdminMenuCategoriesResponse.from(menuCategories);
    }

    public AdminMenuDetailResponse getMenu(Integer shopId, Integer menuId) {
        adminShopRepository.getById(shopId);
        Menu menu = adminMenuRepository.getById(menuId);
        List<MenuCategory> menuCategories = menu.getMenuCategoryMaps()
            .stream()
            .map(MenuCategoryMap::getMenuCategory)
            .toList();
        return AdminMenuDetailResponse.createMenuDetailResponse(menu, menuCategories);
    }

    @Transactional
    public void createShop(AdminCreateShopRequest adminCreateShopRequest) {
        Shop shop = adminCreateShopRequest.toShop();
        Shop savedShop = adminShopRepository.save(shop);
        List<String> categoryNames = List.of("추천 메뉴", "메인 메뉴", "세트 메뉴", "사이드 메뉴");
        for (String categoryName : categoryNames) {
            MenuCategory menuCategory = MenuCategory.builder()
                .shop(savedShop)
                .name(categoryName)
                .build();
            savedShop.getMenuCategories().add(menuCategory);
        }
        for (String imageUrl : adminCreateShopRequest.imageUrls()) {
            ShopImage shopImage = ShopImage.builder()
                .shop(savedShop)
                .imageUrl(imageUrl)
                .build();
            savedShop.getShopImages().add(shopImage);
        }
        for (InnerShopOpen open : adminCreateShopRequest.open()) {
            ShopOpen shopOpen = ShopOpen.builder()
                .shop(savedShop)
                .openTime(open.openTime())
                .closeTime(open.closeTime())
                .dayOfWeek(open.dayOfWeek())
                .closed(open.closed())
                .build();
            savedShop.getShopOpens().add(shopOpen);
        }
        List<ShopCategory> categories = adminShopCategoryRepository.findAllByIdIn(adminCreateShopRequest.categoryIds());
        for (ShopCategory shopCategory : categories) {
            ShopCategoryMap shopCategoryMap = ShopCategoryMap.builder()
                .shopCategory(shopCategory)
                .shop(savedShop)
                .build();
            savedShop.getShopCategories().add(shopCategoryMap);
        }
    }

    @Transactional
    public void createShopCategory(AdminCreateShopCategoryRequest adminCreateShopCategoryRequest) {
        if (adminShopCategoryRepository.findByName(adminCreateShopCategoryRequest.name()).isPresent()) {
            throw ShopCategoryDuplicationException.withDetail("name: " + adminCreateShopCategoryRequest.name());
        }
        ShopCategory shopCategory = adminCreateShopCategoryRequest.toShopCategory();
        adminShopCategoryRepository.save(shopCategory);
    }

    @Transactional
    public void createMenu(Integer shopId, AdminCreateMenuRequest adminCreateMenuRequest) {
        adminShopRepository.getById(shopId);
        Menu menu = adminCreateMenuRequest.toEntity(shopId);
        Menu savedMenu = adminMenuRepository.save(menu);
        for (Integer categoryId : adminCreateMenuRequest.categoryIds()) {
            MenuCategory menuCategory = adminMenuCategoryRepository.getById(categoryId);
            MenuCategoryMap menuCategoryMap = MenuCategoryMap.builder()
                .menuCategory(menuCategory)
                .menu(savedMenu)
                .build();
            savedMenu.getMenuCategoryMaps().add(menuCategoryMap);
        }
        for (String imageUrl : adminCreateMenuRequest.imageUrls()) {
            MenuImage menuImage = MenuImage.builder()
                .imageUrl(imageUrl)
                .menu(savedMenu)
                .build();
            savedMenu.getMenuImages().add(menuImage);
        }
        if (adminCreateMenuRequest.optionPrices() == null) {
            MenuOption menuOption = MenuOption.builder()
                .option(savedMenu.getName())
                .price(adminCreateMenuRequest.singlePrice())
                .menu(menu)
                .build();
            savedMenu.getMenuOptions().add(menuOption);
        } else {
            for (var option : adminCreateMenuRequest.optionPrices()) {
                MenuOption menuOption = MenuOption.builder()
                    .option(option.option())
                    .price(option.price())
                    .menu(menu)
                    .build();
                savedMenu.getMenuOptions().add(menuOption);
            }
        }
    }

    @Transactional
    public void createMenuCategory(Integer shopId, AdminCreateMenuCategoryRequest adminCreateMenuCategoryRequest) {
        Shop shop = adminShopRepository.getById(shopId);
        MenuCategory menuCategory = MenuCategory.builder()
            .shop(shop)
            .name(adminCreateMenuCategoryRequest.name())
            .build();
        adminMenuCategoryRepository.save(menuCategory);
    }

    @Transactional
    public void cancelShopDelete(Integer shopId) {
        Optional<Shop> shop = adminShopRepository.findDeletedShopById(shopId);
        if (shop.isPresent()) {
            shop.get().cancelDelete();
        }
    }

    @Transactional
    public void modifyShop(Integer shopId, AdminModifyShopRequest adminModifyShopRequest) {
        Shop shop = adminShopRepository.getById(shopId);
        shop.modifyShop(
            adminModifyShopRequest.name(),
            adminModifyShopRequest.phone(),
            adminModifyShopRequest.address(),
            adminModifyShopRequest.description(),
            adminModifyShopRequest.delivery(),
            adminModifyShopRequest.deliveryPrice(),
            adminModifyShopRequest.payCard(),
            adminModifyShopRequest.payBank(),
            adminModifyShopRequest.bank(),
            adminModifyShopRequest.accountNumber()
        );
        shop.modifyShopCategories(
            adminShopCategoryRepository.findAllByIdIn(adminModifyShopRequest.categoryIds()),
            entityManager
        );
        shop.modifyShopImages(adminModifyShopRequest.imageUrls(), entityManager);
        shop.modifyAdminShopOpens(adminModifyShopRequest.open(), entityManager);
    }

    @Transactional
    public void modifyShopCategory(Integer categoryId, AdminModifyShopCategoryRequest adminModifyShopCategoryRequest) {
        if (adminShopCategoryRepository.findByName(adminModifyShopCategoryRequest.name()).isPresent()) {
            throw ShopCategoryDuplicationException.withDetail("name: " + adminModifyShopCategoryRequest.name());
        }
        ShopCategory shopCategory = adminShopCategoryRepository.getById(categoryId);
        shopCategory.modifyShopCategory(
            adminModifyShopCategoryRequest.name(),
            adminModifyShopCategoryRequest.imageUrl()
        );
    }

    @Transactional
    public void modifyMenuCategory(Integer shopId, AdminModifyMenuCategoryRequest adminModifyMenuCategoryRequest) {
        adminShopRepository.getById(shopId);
        MenuCategory menuCategory = adminMenuCategoryRepository.getById(adminModifyMenuCategoryRequest.id());
        menuCategory.modifyName(adminModifyMenuCategoryRequest.name());
    }

    @Transactional
    public void modifyMenu(Integer shopId, Integer menuId, AdminModifyMenuRequest adminModifyMenuRequest) {
        Menu menu = adminMenuRepository.getById(menuId);
        adminShopRepository.getById(shopId);
        menu.modifyMenu(
            adminModifyMenuRequest.name(),
            adminModifyMenuRequest.description()
        );
        menu.modifyMenuImages(adminModifyMenuRequest.imageUrls(), entityManager);
        menu.modifyMenuCategories(adminMenuCategoryRepository.findAllByIdIn(adminModifyMenuRequest.categoryIds()),
            entityManager);
        if (adminModifyMenuRequest.isSingle()) {
            menu.adminModifyMenuSingleOptions(adminModifyMenuRequest, entityManager);
        } else {
            List<InnerOptionPrice> optionPrices = adminModifyMenuRequest.optionPrices();
            menu.adminModifyMenuMultipleOptions(optionPrices, entityManager);
        }
    }

    @Transactional
    public void deleteShop(Integer shopId) {
        Shop shop = adminShopRepository.getById(shopId);
        shop.delete();
    }

    @Transactional
    public void deleteShopCategory(Integer categoryId) {
        if (hasShops(categoryId)) {
            throw ShopCategoryNotEmptyException.withDetail("category: " + categoryId);
        }
        adminShopCategoryRepository.deleteById(categoryId);
    }

    private boolean hasShops(Integer categoryId) {
        return adminShopCategoryMapRepository.existsByShopCategoryId(categoryId);
    }

    @Transactional
    public void deleteMenuCategory(Integer shopId, Integer categoryId) {
        MenuCategory menuCategory = adminMenuCategoryRepository.getById(categoryId);
        if (!Objects.equals(menuCategory.getShop().getId(), shopId)) {
            throw new KoinIllegalArgumentException("해당 상점의 카테고리가 아닙니다.");
        }
        adminMenuCategoryRepository.deleteById(categoryId);
    }

    @Transactional
    public void deleteMenu(Integer shopId, Integer menuId) {
        Menu menu = adminMenuRepository.getById(menuId);
        if (!Objects.equals(menu.getShopId(), shopId)) {
            throw new KoinIllegalArgumentException("해당 상점의 카테고리가 아닙니다.");
        }
        adminMenuRepository.deleteById(menuId);
    }

    @Transactional(readOnly = true)
    public AdminShopsReviewsResponse getReviews(
        Integer page,
        Integer limit,
        Boolean isReported,
        Boolean hasUnhandledReport,
        Integer shopId
    ) {
        Long reviewCount = adminShopReviewCustomRepository.countShopReview(shopId, isReported, hasUnhandledReport);
        Criteria criteria = Criteria.of(page, limit, reviewCount.intValue());
        PageRequest pageRequest = PageRequest.of(
            criteria.getPage(),
            criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id")
        );

        Page<ShopReview> reviews = adminShopReviewCustomRepository.findShopReview(
            shopId,
            isReported,
            hasUnhandledReport,
            pageRequest
        );
        return AdminShopsReviewsResponse.of(reviews, criteria);
    }

    @Transactional
    public void modifyShopReviewReportStatus(
        Integer reviewId,
        AdminModifyShopReviewReportStatusRequest adminModifyShopReviewReportStatusRequest
    ) {
        ShopReview shopReview = adminShopReviewRepository.findById(reviewId)
            .orElseThrow(() -> ReviewNotFoundException.withDetail("reviewId: " + reviewId));

        List<ShopReviewReport> unhandledReports = shopReview.getReports().stream()
            .filter(report -> report.getReportStatus() == ReportStatus.UNHANDLED)
            .toList();

        unhandledReports.forEach(
            report -> report.modifyReportStatus(adminModifyShopReviewReportStatusRequest.reportStatus()));
    }

    @Transactional
    public void deleteShopReview(Integer reviewId) {
        ShopReview shopReview = adminShopReviewRepository.findById(reviewId)
            .orElseThrow(() -> ReviewNotFoundException.withDetail("reviewId: " + reviewId));

        shopReview.getReports().forEach(report -> report.modifyReportStatus(DELETED));
        shopReview.deleteReview();
    }
}
