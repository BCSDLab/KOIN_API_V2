package in.koreatech.koin.admin.shop.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.event.ShopImagesDeletedEvent;
import in.koreatech.koin.admin.shop.dto.menu.*;
import in.koreatech.koin.admin.shop.repository.menu.AdminMenuCategoryRepository;
import in.koreatech.koin.admin.shop.repository.menu.AdminMenuRepository;
import in.koreatech.koin.admin.shop.repository.shop.AdminShopRepository;
import in.koreatech.koin.domain.shop.cache.aop.RefreshShopsCache;
import in.koreatech.koin.domain.shop.model.menu.*;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminShopMenuService {

    private final EntityManager entityManager;
    private final AdminShopRepository adminShopRepository;
    private final AdminMenuRepository adminMenuRepository;
    private final AdminMenuCategoryRepository adminMenuCategoryRepository;
    private final ApplicationEventPublisher eventPublisher;

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
    @RefreshShopsCache
    public void createMenu(Integer shopId, AdminCreateMenuRequest request) {
        Shop shop = adminShopRepository.getById(shopId);
        Menu menu = request.toEntity(shop);
        Menu savedMenu = adminMenuRepository.save(menu);

        request.categoryIds().forEach(categoryId -> {
            MenuCategory menuCategory = adminMenuCategoryRepository.getById(categoryId);
            MenuCategoryMap menuCategoryMap = MenuCategoryMap.builder()
                .menuCategory(menuCategory)
                .menu(savedMenu)
                .build();
            savedMenu.getMenuCategoryMaps().add(menuCategoryMap);
        });

        request.imageUrls().forEach(imageUrl -> {
            MenuImage menuImage = MenuImage.builder()
                .imageUrl(imageUrl)
                .menu(savedMenu)
                .build();
            savedMenu.getMenuImages().add(menuImage);
        });

        if (request.optionPrices() == null) {
            MenuOption menuOption = MenuOption.builder()
                .option(savedMenu.getName())
                .price(request.singlePrice())
                .menu(menu)
                .build();
            savedMenu.getMenuOptions().add(menuOption);
        } else {
            request.optionPrices().forEach(option -> {
                MenuOption menuOption = MenuOption.builder()
                    .option(option.option())
                    .price(option.price())
                    .menu(menu)
                    .build();
                savedMenu.getMenuOptions().add(menuOption);
            });
        }
    }

    @Transactional
    public void createMenuCategory(Integer shopId, AdminCreateMenuCategoryRequest request) {
        Shop shop = adminShopRepository.getById(shopId);
        MenuCategory menuCategory = MenuCategory.builder()
            .shop(shop)
            .name(request.name())
            .build();
        adminMenuCategoryRepository.save(menuCategory);
    }

    @Transactional
    public void modifyMenuCategory(Integer shopId, AdminModifyMenuCategoryRequest request) {
        adminShopRepository.getById(shopId);
        MenuCategory menuCategory = adminMenuCategoryRepository.getById(request.id());
        menuCategory.modifyName(request.name());
    }

    @Transactional
    @RefreshShopsCache
    public void modifyMenu(Integer shopId, Integer menuId, AdminModifyMenuRequest request) {
        Menu menu = adminMenuRepository.getById(menuId);
        adminShopRepository.getById(shopId);
        menu.modifyMenu(request.name(), request.description());
        menu.modifyMenuImages(request.imageUrls(), entityManager);
        menu.modifyMenuCategories(adminMenuCategoryRepository.findAllByIdIn(request.categoryIds()), entityManager);
        menu.modifyOptions(request.toMenuOption(menu), entityManager);
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
    @RefreshShopsCache
    public void deleteMenu(Integer shopId, Integer menuId) {
        Menu menu = adminMenuRepository.getById(menuId);
        if (!Objects.equals(menu.getShop().getId(), shopId)) {
            throw new KoinIllegalArgumentException("해당 상점의 메뉴가 아닙니다.");
        }
        List<String> imageUrls = menu.getMenuImages().stream()
            .map(MenuImage::getImageUrl)
            .toList();
        adminMenuRepository.deleteById(menuId);
        eventPublisher.publishEvent(new ShopImagesDeletedEvent(imageUrls));
    }
}
