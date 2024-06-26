package in.koreatech.koin.admin.shop.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.shop.dto.AdminCreateMenuCategoryRequest;
import in.koreatech.koin.admin.shop.dto.AdminCreateMenuRequest;
import in.koreatech.koin.admin.shop.dto.AdminCreateShopCategoryRequest;
import in.koreatech.koin.admin.shop.dto.AdminCreateShopRequest;
import in.koreatech.koin.admin.shop.dto.AdminCreateShopRequest.InnerShopOpen;
import in.koreatech.koin.admin.shop.dto.AdminMenuCategoriesResponse;
import in.koreatech.koin.admin.shop.dto.AdminMenuDetailResponse;
import in.koreatech.koin.admin.shop.dto.AdminModifyMenuCategoryRequest;
import in.koreatech.koin.admin.shop.dto.AdminModifyMenuRequest;
import in.koreatech.koin.admin.shop.dto.AdminModifyMenuRequest.InnerOptionPrice;
import in.koreatech.koin.admin.shop.dto.AdminModifyShopCategoryRequest;
import in.koreatech.koin.admin.shop.dto.AdminModifyShopRequest;
import in.koreatech.koin.admin.shop.dto.AdminShopCategoriesResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopCategoryResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopMenuResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopsResponse;
import in.koreatech.koin.admin.shop.exception.ShopCategoryDuplicationException;
import in.koreatech.koin.admin.shop.repository.AdminEventArticleRepository;
import in.koreatech.koin.admin.shop.repository.AdminMenuCategoryMapRepository;
import in.koreatech.koin.admin.shop.repository.AdminMenuCategoryRepository;
import in.koreatech.koin.admin.shop.repository.AdminMenuDetailRepository;
import in.koreatech.koin.admin.shop.repository.AdminMenuImageRepository;
import in.koreatech.koin.admin.shop.repository.AdminMenuRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopCategoryMapRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopCategoryRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopImageRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopOpenRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopRepository;
import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.MenuImage;
import in.koreatech.koin.domain.shop.model.MenuOption;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.model.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.ShopImage;
import in.koreatech.koin.domain.shop.model.ShopOpen;
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

    public AdminShopCategoriesResponse getShopCategories(Integer page, Integer limit, Boolean isDeleted) {
        Integer total = adminShopCategoryRepository.countAllByIsDeleted(isDeleted);
        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));
        Page<ShopCategory> result = adminShopCategoryRepository.findAllByIsDeleted(isDeleted, pageRequest);
        return AdminShopCategoriesResponse.of(result, criteria);
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
        List<ShopCategory> categories = adminShopCategoryRepository.findAllByIdIn(adminCreateShopRequest.categoryIds());
        for (ShopCategory shopCategory : categories) {
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
            adminMenuCategoryMapRepository.save(menuCategoryMap);
        }
        for (String imageUrl : adminCreateMenuRequest.imageUrls()) {
            MenuImage menuImage = MenuImage.builder()
                .imageUrl(imageUrl)
                .menu(savedMenu)
                .build();
            adminMenuImageRepository.save(menuImage);
        }
        if (adminCreateMenuRequest.optionPrices() == null) {
            MenuOption menuOption = MenuOption.builder()
                .option(savedMenu.getName())
                .price(adminCreateMenuRequest.singlePrice())
                .menu(menu)
                .build();
            adminMenuDetailRepository.save(menuOption);
        } else {
            for (var option : adminCreateMenuRequest.optionPrices()) {
                MenuOption menuOption = MenuOption.builder()
                    .option(option.option())
                    .price(option.price())
                    .menu(menu)
                    .build();
                adminMenuDetailRepository.save(menuOption);
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
        if(shop.isPresent()) {
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
        menu.modifyMenuCategories(adminMenuCategoryRepository.findAllByIdIn(adminModifyMenuRequest.categoryIds()), entityManager);
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
        ShopCategory shopCategory = adminShopCategoryRepository.getById(categoryId);
        shopCategory.delete();
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
}
