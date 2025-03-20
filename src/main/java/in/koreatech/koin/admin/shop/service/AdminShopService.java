package in.koreatech.koin.admin.shop.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.shop.dto.shop.AdminCreateShopCategoryRequest;
import in.koreatech.koin.admin.shop.dto.shop.AdminCreateShopRequest;
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
import in.koreatech.koin.admin.shop.repository.shop.AdminEventArticleRepository;
import in.koreatech.koin.admin.shop.repository.shop.AdminShopCategoryMapRepository;
import in.koreatech.koin.admin.shop.repository.shop.AdminShopCategoryRepository;
import in.koreatech.koin.admin.shop.repository.shop.AdminShopParentCategoryRepository;
import in.koreatech.koin.admin.shop.repository.shop.AdminShopRepository;
import in.koreatech.koin.domain.shop.exception.ShopNotFoundException;
import in.koreatech.koin.domain.shop.model.menu.MenuCategory;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.model.shop.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.shop.ShopImage;
import in.koreatech.koin.domain.shop.model.shop.ShopOpen;
import in.koreatech.koin.domain.shop.model.shop.ShopParentCategory;
import in.koreatech.koin._common.model.Criteria;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminShopService {

    private final Clock clock;
    private final EntityManager entityManager;
    private final AdminShopRepository adminShopRepository;
    private final AdminEventArticleRepository adminEventArticleRepository;
    private final AdminShopCategoryRepository adminShopCategoryRepository;
    private final AdminShopCategoryMapRepository adminShopCategoryMapRepository;
    private final AdminShopParentCategoryRepository adminShopParentCategoryRepository;

    public AdminShopsResponse getShops(Integer page, Integer limit, Boolean isDeleted) {
        Integer total = adminShopRepository.countAllByIsDeleted(isDeleted);
        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(
            criteria.getPage(),
            criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id")
        );
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
    public void createShop(AdminCreateShopRequest request) {
        ShopCategory shopMainCategory = adminShopCategoryRepository.getById(request.mainCategoryId());
        Shop shop = request.toShop(shopMainCategory);
        Shop savedShop = adminShopRepository.save(shop);

        List<String> categoryNames = List.of("추천 메뉴", "메인 메뉴", "세트 메뉴", "사이드 메뉴");
        categoryNames.forEach(categoryName -> {
            MenuCategory menuCategory = MenuCategory.builder()
                .shop(savedShop)
                .name(categoryName)
                .build();
            savedShop.getMenuCategories().add(menuCategory);
        });

        request.imageUrls().forEach(imageUrl -> {
            ShopImage shopImage = ShopImage.builder()
                .shop(savedShop)
                .imageUrl(imageUrl)
                .build();
            savedShop.getShopImages().add(shopImage);
        });

        request.open().forEach(open -> {
            ShopOpen shopOpen = ShopOpen.builder()
                .shop(savedShop)
                .openTime(open.openTime())
                .closeTime(open.closeTime())
                .dayOfWeek(open.dayOfWeek())
                .closed(open.closed())
                .build();
            savedShop.getShopOpens().add(shopOpen);
        });

        List<ShopCategory> categories = adminShopCategoryRepository.findAllByIdIn(request.categoryIds());
        categories.forEach(shopCategory -> {
            ShopCategoryMap shopCategoryMap = ShopCategoryMap.builder()
                .shopCategory(shopCategory)
                .shop(savedShop)
                .build();
            savedShop.getShopCategories().add(shopCategoryMap);
        });
    }

    @Transactional
    public void createShopCategory(AdminCreateShopCategoryRequest request) {
        String categoryName = request.name();
        if (adminShopCategoryRepository.existsByName(categoryName)) {
            throw ShopCategoryDuplicationException.withDetail("중복되는 상점 카테고리명이 존재합니다.: " + categoryName);
        }
        Integer maxOrderIndex = adminShopCategoryRepository.findMaxOrderIndex();
        ShopParentCategory shopParentCategory = adminShopParentCategoryRepository.getById(request.parentCategoryId());
        ShopCategory shopCategory = request.toShopCategory(maxOrderIndex, shopParentCategory);
        adminShopCategoryRepository.save(shopCategory);
    }

    @Transactional
    public void cancelShopDelete(Integer shopId) {
        Shop shop = adminShopRepository.findDeletedShopById(shopId)
            .orElseThrow(() -> new ShopNotFoundException("해당 상점이 존재하지 않습니다.: " + shopId));
        shop.cancelDelete();
    }

    @Transactional
    public void modifyShop(Integer shopId, AdminModifyShopRequest request) {
        Shop shop = adminShopRepository.getById(shopId);
        ShopCategory shopMainCategory = adminShopCategoryRepository.getById(request.mainCategoryId());
        shop.modifyShop(
            request.name(),
            request.phone(),
            request.address(),
            request.description(),
            request.delivery(),
            request.deliveryPrice(),
            request.payCard(),
            request.payBank(),
            request.bank(),
            request.accountNumber(),
            shopMainCategory
        );
        shop.modifyShopCategories(
            adminShopCategoryRepository.findAllByIdIn(request.categoryIds()),
            entityManager
        );
        shop.modifyShopImages(request.imageUrls(), entityManager);
        shop.modifyShopOpens(request.toShopOpens(shop), entityManager);
    }

    @Transactional
    public void modifyShopCategory(Integer categoryId, AdminModifyShopCategoryRequest request) {
        validateExistCategoryName(request.name(), categoryId);
        ShopCategory shopCategory = adminShopCategoryRepository.getById(categoryId);
        ShopParentCategory shopParentCategory = adminShopParentCategoryRepository.getById(request.parentCategoryId());
        shopCategory.modifyShopCategory(
            request.name(),
            request.imageUrl(),
            shopParentCategory,
            request.eventBannerImageUrl()
        );
    }

    @Transactional
    public void modifyShopCategoriesOrder(AdminModifyShopCategoriesOrderRequest request) {
        Map<Integer, ShopCategory> shopCategoryMap = adminShopCategoryRepository.findAll().stream()
            .collect(Collectors.toMap(ShopCategory::getId, category -> category));

        List<Integer> shopCategoryIds = request.shopCategoryIds();
        Set<Integer> shopCategoryIdsSet = new HashSet<>(shopCategoryIds);
        if (!Objects.equals(shopCategoryMap.keySet(), shopCategoryIdsSet)) {
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
        validateHasShops(categoryId);
        adminShopCategoryRepository.deleteById(categoryId);
    }

    private void validateExistCategoryName(String name, Integer categoryId) {
        if (adminShopCategoryRepository.existsByNameAndIdNot(name, categoryId)) {
            throw ShopCategoryDuplicationException.withDetail("중복되는 상점 카테고리명이 존재합니다.: " + name);
        }
    }

    private void validateHasShops(Integer categoryId) {
        if (adminShopCategoryMapRepository.existsByShopCategoryId(categoryId)) {
            throw ShopCategoryNotEmptyException.withDetail("카테고리에 상점이 존재합니다." + categoryId);
        }
    }
}
