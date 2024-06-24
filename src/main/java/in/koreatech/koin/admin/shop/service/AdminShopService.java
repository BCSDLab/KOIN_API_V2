package in.koreatech.koin.admin.shop.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.shop.dto.AdminCreateShopCategoryRequest;
import in.koreatech.koin.admin.shop.dto.AdminCreateShopRequest;
import in.koreatech.koin.admin.shop.dto.AdminCreateShopRequest.InnerShopOpen;
import in.koreatech.koin.admin.shop.dto.AdminModifyShopCategoryRequest;
import in.koreatech.koin.admin.shop.dto.AdminModifyShopRequest;
import in.koreatech.koin.admin.shop.dto.AdminShopCategoriesResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopCategoryResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopsResponse;
import in.koreatech.koin.admin.shop.exception.ShopCategoryDuplicationException;
import in.koreatech.koin.admin.shop.repository.AdminEventArticleRepository;
import in.koreatech.koin.admin.shop.repository.AdminMenuCategoryRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopCategoryMapRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopCategoryRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopImageRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopOpenRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopRepository;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.model.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.ShopImage;
import in.koreatech.koin.domain.shop.model.ShopOpen;
import in.koreatech.koin.global.model.Criteria;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminShopService {

    private final EntityManager entityManager;
    private final Clock clock;
    private final AdminShopRepository adminShopRepository;
    private final AdminShopCategoryRepository adminShopCategoryRepository;
    private final AdminEventArticleRepository adminEventArticleRepository;
    private final AdminMenuCategoryRepository adminMenuCategoryRepository;
    private final AdminShopImageRepository adminShopImageRepository;
    private final AdminShopOpenRepository adminShopOpenRepository;
    private final AdminShopCategoryMapRepository adminShopCategoryMapRepository;

    public AdminShopsResponse getShops(Integer page, Integer limit, Boolean isDeleted) {
        Integer total = adminShopRepository.countAllByIsDeleted(isDeleted);
        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));
        Page<Shop> result = adminShopRepository.findAllByIsDeleted(isDeleted, pageRequest);
        return AdminShopsResponse.from(result, criteria);
    }

    public AdminShopResponse getShop(Integer shopId) {
        Shop shop = adminShopRepository.getById(shopId);
        boolean eventDuration = adminEventArticleRepository.isDurationEvent(shopId, LocalDate.now(clock));
        return AdminShopResponse.from(shop, eventDuration);
    }

    public AdminShopCategoriesResponse getShopCategories(Integer page, Integer limit, Boolean isDeleted) {
        Integer total = adminShopCategoryRepository.countAllByIsDeleted(isDeleted);
        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));
        Page<ShopCategory> result = adminShopCategoryRepository.findAllByIsDeleted(isDeleted, pageRequest);
        return AdminShopCategoriesResponse.of(result.getContent(), criteria.getPage() + 1, result.getTotalPages());
    }

    public AdminShopCategoryResponse getShopCategory(Integer categoryId) {
        ShopCategory shopCategory = adminShopCategoryRepository.getById(categoryId);
        return AdminShopCategoryResponse.from(shopCategory);
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
            adminMenuCategoryRepository.save(menuCategory);
        }
        for (String imageUrl : adminCreateShopRequest.imageUrls()) {
            ShopImage shopImage = ShopImage.builder()
                .shop(savedShop)
                .imageUrl(imageUrl)
                .build();
            adminShopImageRepository.save(shopImage);
        }
        for (InnerShopOpen open : adminCreateShopRequest.open()) {
            ShopOpen shopOpen = ShopOpen.builder()
                .shop(savedShop)
                .openTime(open.openTime())
                .closeTime(open.closeTime())
                .dayOfWeek(open.dayOfWeek())
                .closed(open.closed())
                .build();
            adminShopOpenRepository.save(shopOpen);
        }
        List<ShopCategory> Categories = adminShopCategoryRepository.findAllByIdIn(adminCreateShopRequest.categoryIds());
        for (ShopCategory shopCategory : Categories) {
            ShopCategoryMap shopCategoryMap = ShopCategoryMap.builder()
                .shopCategory(shopCategory)
                .shop(savedShop)
                .build();
            adminShopCategoryMapRepository.save(shopCategoryMap);
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
            adminModifyShopRequest.payBank()
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
    public void deleteShop(Integer shopId) {
        Shop shop = adminShopRepository.getById(shopId);
        shop.delete();
    }

    @Transactional
    public void deleteShopCategory(Integer categoryId) {
        ShopCategory shopCategory = adminShopCategoryRepository.getById(categoryId);
        shopCategory.delete();
    }
}
