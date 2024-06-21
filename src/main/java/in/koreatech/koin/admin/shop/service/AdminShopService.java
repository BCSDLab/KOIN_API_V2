package in.koreatech.koin.admin.shop.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.shop.dto.AdminCreateMenuCategoryRequest;
import in.koreatech.koin.admin.shop.dto.AdminCreateMenuRequest;
import in.koreatech.koin.admin.shop.dto.AdminMenuCategoriesResponse;
import in.koreatech.koin.admin.shop.dto.AdminMenuDetailResponse;
import in.koreatech.koin.admin.shop.dto.AdminModifyMenuCategoryRequest;
import in.koreatech.koin.admin.shop.dto.AdminModifyMenuRequest;
import in.koreatech.koin.admin.shop.dto.AdminShopMenuResponse;
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
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminShopService {

    private final EntityManager entityManager;
    private final AdminShopRepository adminShopRepository;
    private final AdminMenuRepository adminMenuRepository;
    private final AdminMenuCategoryRepository adminMenuCategoryRepository;
    private final AdminShopImageRepository adminShopImageRepository;
    private final AdminShopOpenRepository adminShopOpenRepository;
    private final AdminMenuCategoryMapRepository adminMenuCategoryMapRepository;
    private final AdminShopCategoryRepository adminShopCategoryRepository;
    private final AdminShopCategoryMapRepository adminShopCategoryMapRepository;
    private final AdminMenuImageRepository adminMenuImageRepository;
    private final AdminMenuDetailRepository adminMenuDetailRepository;
    private final ShopRepository shopRepository;

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
    public void createMenu(Integer shopId, AdminCreateMenuRequest adminCreateMenuRequest) {
        shopRepository.getById(shopId);
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
        Shop shop = shopRepository.getById(shopId);
        MenuCategory menuCategory = MenuCategory.builder()
            .shop(shop)
            .name(adminCreateMenuCategoryRequest.name())
            .build();
        adminMenuCategoryRepository.save(menuCategory);
    }

    @Transactional
    public void cancelShopDelete(Integer shopId) {
        Shop shop = shopRepository.getById(shopId);
        shop.cancelDelete();
    }

    @Transactional
    public void modifyMenuCategory(Integer shopId, AdminModifyMenuCategoryRequest adminModifyMenuCategoryRequest) {
        shopRepository.getById(shopId);
        MenuCategory menuCategory = adminMenuCategoryRepository.getById(adminModifyMenuCategoryRequest.id());
        menuCategory.modifyName(adminModifyMenuCategoryRequest.name());
    }

    @Transactional
    public void modifyMenu(Integer shopId, Integer menuId, AdminModifyMenuRequest adminModifyMenuRequest) {
        Menu menu = adminMenuRepository.getById(menuId);
        shopRepository.getById(shopId);
        menu.modifyMenu(
            adminModifyMenuRequest.name(),
            adminModifyMenuRequest.description()
        );
        menu.modifyMenuImages(adminModifyMenuRequest.imageUrls(), entityManager);
        menu.modifyMenuCategories(adminMenuCategoryRepository.findAllByIdIn(adminModifyMenuRequest.categoryIds()), entityManager);
        if (adminModifyMenuRequest.isSingle()) {
            menu.adminModifyMenuSingleOptions(adminModifyMenuRequest, entityManager);
        } else {
            menu.adminModifyMenuMultipleOptions(adminModifyMenuRequest.optionPrices(), entityManager);
        }
    }

    @Transactional
    public void deleteMenuCategory(Integer shopId, Integer categoryId) {
        shopRepository.getById(shopId);
        adminMenuCategoryRepository.getById(categoryId);
        adminMenuCategoryRepository.deleteById(categoryId);
    }

    @Transactional
    public void deleteMenu(Integer shopId, Integer menuId) {
        shopRepository.getById(shopId);
        adminMenuRepository.getById(menuId);
        adminMenuRepository.deleteById(menuId);
    }
}
