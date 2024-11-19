package in.koreatech.koin.admin.shop.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.shop.dto.shop.AdminCreateShopCategoryRequest;
import in.koreatech.koin.admin.shop.dto.shop.AdminCreateShopRequest;
import in.koreatech.koin.admin.shop.dto.shop.AdminCreateShopRequest.InnerShopOpen;
import in.koreatech.koin.admin.shop.dto.shop.AdminModifyShopCategoriesOrderRequest;
import in.koreatech.koin.admin.shop.dto.shop.AdminModifyShopCategoryRequest;
import in.koreatech.koin.admin.shop.dto.shop.AdminModifyShopRequest;
import in.koreatech.koin.admin.shop.dto.shop.AdminShopCategoryResponse;
import in.koreatech.koin.admin.shop.dto.shop.AdminShopParentCategoryResponse;
import in.koreatech.koin.admin.shop.dto.shop.AdminShopResponse;
import in.koreatech.koin.admin.shop.dto.shop.AdminShopsResponse;
import in.koreatech.koin.admin.shop.exception.ShopCategoryDuplicationException;
import in.koreatech.koin.admin.shop.exception.ShopCategoryIllegalArgumentException;
import in.koreatech.koin.admin.shop.exception.ShopCategoryNotEmptyException;
import in.koreatech.koin.admin.shop.repository.AdminEventArticleRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopCategoryMapRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopCategoryRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopParentCategoryRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopRepository;
import in.koreatech.koin.domain.shop.model.menu.MenuCategory;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.model.shop.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.shop.ShopImage;
import in.koreatech.koin.domain.shop.model.shop.ShopOpen;
import in.koreatech.koin.domain.shop.model.shop.ShopParentCategory;
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
    private final AdminShopCategoryMapRepository adminShopCategoryMapRepository;
    private final AdminShopCategoryRepository adminShopCategoryRepository;
    private final AdminShopParentCategoryRepository adminShopParentCategoryRepository;
    private final AdminShopRepository adminShopRepository;

    public AdminShopsResponse getShops(Integer page, Integer limit, Boolean isDeleted) {
        Integer total = adminShopRepository.countAllByIsDeleted(isDeleted);
        Criteria criteria = Criteria.of(page, limit, total);
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), sort);
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

    public List<AdminShopParentCategoryResponse> getShopParentCategories() {
        List<ShopParentCategory> shopParentCategories = adminShopParentCategoryRepository.findAll();
        return shopParentCategories.stream()
            .map(AdminShopParentCategoryResponse::from)
            .toList();
    }

    @Transactional
    public void createShop(AdminCreateShopRequest adminCreateShopRequest) {
        ShopCategory shopMainCategory = adminShopCategoryRepository.getById(adminCreateShopRequest.mainCategoryId());
        Shop shop = adminCreateShopRequest.toShop(shopMainCategory);
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
        Integer maxOrderIndex = adminShopCategoryRepository.findMaxOrderIndex();
        ShopParentCategory shopParentCategory =
            adminShopParentCategoryRepository.getById(adminCreateShopCategoryRequest.parentCategoryId());
        ShopCategory shopCategory = adminCreateShopCategoryRequest.toShopCategory(maxOrderIndex, shopParentCategory);
        adminShopCategoryRepository.save(shopCategory);
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
        ShopCategory shopMainCategory = adminShopCategoryRepository.getById(adminModifyShopRequest.mainCategoryId());
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
            adminModifyShopRequest.accountNumber(),
            shopMainCategory
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
        validateExistCategoryName(adminModifyShopCategoryRequest.name(), categoryId);
        ShopCategory shopCategory = adminShopCategoryRepository.getById(categoryId);
        ShopParentCategory shopParentCategory =
            adminShopParentCategoryRepository.getById(adminModifyShopCategoryRequest.parentCategoryId());
        shopCategory.modifyShopCategory(
            adminModifyShopCategoryRequest.name(),
            adminModifyShopCategoryRequest.imageUrl(),
            shopParentCategory,
            adminModifyShopCategoryRequest.eventBannerImageUrl()
        );
    }

    @Transactional
    public void modifyShopCategoriesOrder(AdminModifyShopCategoriesOrderRequest adminModifyShopCategoriesOrderRequest) {
        Map<Integer, ShopCategory> shopCategoryMap = adminShopCategoryRepository.findAll().stream()
            .collect(Collectors.toMap(ShopCategory::getId, category -> category));

        List<Integer> shopCategoryIds = adminModifyShopCategoriesOrderRequest.shopCategoryIds();
        if (!Objects.equals(shopCategoryMap.keySet(), new HashSet<>(shopCategoryIds))) {
            throw ShopCategoryIllegalArgumentException.withDetail("카테고리 목록이 잘못되었습니다.");
        }

        for (int i = 0; i < shopCategoryIds.size(); i++) {
            ShopCategory shopCategory = shopCategoryMap.get(shopCategoryIds.get(i));
            shopCategory.modifyOrderIndex(i);
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

    private void validateExistCategoryName(String name, Integer categoryId) {
        if (adminShopCategoryRepository.existsByNameAndIdNot(name, categoryId)) {
            throw ShopCategoryDuplicationException.withDetail("중복되는 상점 카테고리명이 있습니다.: " + name);
        }
    }

    private boolean hasShops(Integer categoryId) {
        return adminShopCategoryMapRepository.existsByShopCategoryId(categoryId);
    }
}
