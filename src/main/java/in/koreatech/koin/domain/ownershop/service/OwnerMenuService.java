package in.koreatech.koin.domain.ownershop.service;

import in.koreatech.koin.domain.shop.cache.aop.RefreshShopsCache;
import in.koreatech.koin.domain.shop.dto.menu.request.CreateCategoryRequest;
import in.koreatech.koin.domain.shop.dto.menu.request.CreateMenuRequest;
import in.koreatech.koin.domain.shop.dto.menu.request.ModifyCategoryRequest;
import in.koreatech.koin.domain.shop.dto.menu.request.ModifyMenuRequest;
import in.koreatech.koin.domain.shop.dto.menu.response.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.menu.response.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.menu.response.ShopMenuResponse;
import in.koreatech.koin.domain.shop.model.menu.Menu;
import in.koreatech.koin.domain.shop.model.menu.MenuCategory;
import in.koreatech.koin.domain.shop.model.menu.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.menu.MenuCategoryRepository;
import in.koreatech.koin.domain.shop.repository.menu.MenuRepository;
import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerMenuService {

    private final EntityManager entityManager;
    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final OwnerShopUtilService ownerShopUtilService;

    public MenuDetailResponse getMenuByMenuId(Integer ownerId, Integer menuId) {
        Menu menu = menuRepository.getById(menuId);
        ownerShopUtilService.getOwnerShopById(menu.getShop().getId(), ownerId);
        List<MenuCategory> menuCategories = menu.getMenuCategoryMaps()
            .stream()
            .map(MenuCategoryMap::getMenuCategory)
            .toList();
        return MenuDetailResponse.createMenuDetailResponse(menu, menuCategories);
    }

    public ShopMenuResponse getMenus(Integer shopId, Integer ownerId) {
        Shop shop = ownerShopUtilService.getOwnerShopById(shopId, ownerId);
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByShopId(shop.getId());
        Collections.sort(menuCategories);
        return ShopMenuResponse.from(menuCategories);
    }

    public MenuCategoriesResponse getCategories(Integer shopId, Integer ownerId) {
        Shop shop = ownerShopUtilService.getOwnerShopById(shopId, ownerId);
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByShopId(shop.getId());
        return MenuCategoriesResponse.from(menuCategories);
    }

    @Transactional
    @RefreshShopsCache
    public void deleteMenuByMenuId(Integer ownerId, Integer menuId) {
        Menu menu = menuRepository.getById(menuId);
        ownerShopUtilService.getOwnerShopById(menu.getShop().getId(), ownerId);
        menuRepository.deleteById(menuId);
    }

    @Transactional
    public void deleteCategory(Integer ownerId, Integer categoryId) {
        MenuCategory menuCategory = menuCategoryRepository.getById(categoryId);
        ownerShopUtilService.getOwnerShopById(menuCategory.getShop().getId(), ownerId);
        menuCategoryRepository.deleteById(categoryId);
    }

    @Transactional
    @RefreshShopsCache
    public void createMenu(Integer shopId, Integer ownerId, CreateMenuRequest createMenuRequest) {
        Shop shop = ownerShopUtilService.getOwnerShopById(shopId, ownerId);
        Menu savedMenu = menuRepository.save(createMenuRequest.toEntity(shop));
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByIdIn(createMenuRequest.categoryIds());
        savedMenu.addMenuCategories(menuCategories);
        savedMenu.addMenuImages(createMenuRequest.imageUrls());
        savedMenu.addMenuOptions(createMenuRequest.toMenuOption(savedMenu));
    }

    @Transactional
    public void createMenuCategory(Integer shopId, Integer ownerId, CreateCategoryRequest createCategoryRequest) {
        Shop shop = ownerShopUtilService.getOwnerShopById(shopId, ownerId);
        MenuCategory menuCategory = MenuCategory.builder()
            .shop(shop)
            .name(createCategoryRequest.name())
            .build();
        menuCategoryRepository.save(menuCategory);
    }

    @Transactional
    @RefreshShopsCache
    public void modifyMenu(Integer ownerId, Integer menuId, ModifyMenuRequest modifyMenuRequest) {
        Menu menu = menuRepository.getById(menuId);
        ownerShopUtilService.getOwnerShopById(menu.getShop().getId(), ownerId);
        menu.modifyMenu(modifyMenuRequest.name(), modifyMenuRequest.description());
        menu.modifyMenuImages(modifyMenuRequest.imageUrls(), entityManager);
        menu.modifyMenuCategories(menuCategoryRepository.findAllByIdIn(modifyMenuRequest.categoryIds()), entityManager);
        menu.modifyOptions(modifyMenuRequest.toMenuOption(menu), entityManager);
    }

    @Transactional
    public void modifyCategory(Integer ownerId, Integer categoryId, ModifyCategoryRequest modifyCategoryRequest) {
        MenuCategory menuCategory = menuCategoryRepository.getById(categoryId);
        ownerShopUtilService.getOwnerShopById(menuCategory.getShop().getId(), ownerId);
        menuCategory.modifyName(modifyCategoryRequest.name());
    }
}
