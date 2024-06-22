package in.koreatech.koin.admin.shop.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
import in.koreatech.koin.admin.shop.repository.AdminShopRepository;
import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.MenuImage;
import in.koreatech.koin.domain.shop.model.MenuOption;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
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
    private final AdminMenuCategoryMapRepository adminMenuCategoryMapRepository;
    private final AdminMenuImageRepository adminMenuImageRepository;
    private final AdminMenuDetailRepository adminMenuDetailRepository;

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
            menu.adminModifyMenuMultipleOptions(adminModifyMenuRequest.optionPrices(), entityManager);
        }
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
