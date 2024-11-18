package in.koreatech.koin.domain.ownershop.service;

import in.koreatech.koin.domain.shop.dto.menu.CreateCategoryRequest;
import in.koreatech.koin.domain.shop.dto.menu.CreateMenuRequest;
import in.koreatech.koin.domain.shop.dto.menu.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.menu.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.menu.ModifyCategoryRequest;
import in.koreatech.koin.domain.shop.dto.menu.ModifyMenuRequest;
import in.koreatech.koin.domain.shop.dto.menu.ShopMenuResponse;
import in.koreatech.koin.domain.shop.model.menu.Menu;
import in.koreatech.koin.domain.shop.model.menu.MenuCategory;
import in.koreatech.koin.domain.shop.model.menu.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.menu.MenuImage;
import in.koreatech.koin.domain.shop.model.menu.MenuOption;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.menu.MenuCategoryRepository;
import in.koreatech.koin.domain.shop.repository.menu.MenuRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
    private final OwnerUtiltService ownerUtiltService;

    public MenuDetailResponse getMenuByMenuId(Integer ownerId, Integer menuId) {
        Menu menu = menuRepository.getById(menuId);
        ownerUtiltService.getOwnerShopById(menu.getShop().getId(), ownerId);
        List<MenuCategory> menuCategories = menu.getMenuCategoryMaps()
            .stream()
            .map(MenuCategoryMap::getMenuCategory)
            .toList();
        return MenuDetailResponse.createMenuDetailResponse(menu, menuCategories);
    }

    public ShopMenuResponse getMenus(Integer shopId, Integer ownerId) {
        Shop shop = ownerUtiltService.getOwnerShopById(shopId, ownerId);
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByShopId(shop.getId());
        Collections.sort(menuCategories);
        return ShopMenuResponse.from(menuCategories);
    }

    public MenuCategoriesResponse getCategories(Integer shopId, Integer ownerId) {
        Shop shop = ownerUtiltService.getOwnerShopById(shopId, ownerId);
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByShopId(shop.getId());
        return MenuCategoriesResponse.from(menuCategories);
    }

    @Transactional
    public void deleteMenuByMenuId(Integer ownerId, Integer menuId) {
        Menu menu = menuRepository.getById(menuId);
        ownerUtiltService.getOwnerShopById(menu.getShop().getId(), ownerId);
        menuRepository.deleteById(menuId);
    }

    @Transactional
    public void deleteCategory(Integer ownerId, Integer categoryId) {
        MenuCategory menuCategory = menuCategoryRepository.getById(categoryId);
        ownerUtiltService.getOwnerShopById(menuCategory.getShop().getId(), ownerId);
        menuCategoryRepository.deleteById(categoryId);
    }

    @Transactional
    public void createMenu(Integer shopId, Integer ownerId, CreateMenuRequest createMenuRequest) {
        Shop shop = ownerUtiltService.getOwnerShopById(shopId, ownerId);
        Menu savedMenu = menuRepository.save(createMenuRequest.toEntity(shop));
        for (Integer categoryId : createMenuRequest.categoryIds()) {
            MenuCategory menuCategory = menuCategoryRepository.getById(categoryId);
            MenuCategoryMap menuCategoryMap = MenuCategoryMap.builder()
                .menuCategory(menuCategory)
                .menu(savedMenu)
                .build();
            savedMenu.getMenuCategoryMaps().add(menuCategoryMap);
        }
        for (String imageUrl : createMenuRequest.imageUrls()) {
            MenuImage menuImage = MenuImage.builder()
                .imageUrl(imageUrl)
                .menu(savedMenu)
                .build();
            savedMenu.getMenuImages().add(menuImage);
        }
        if (createMenuRequest.optionPrices() == null) {
            MenuOption menuOption = MenuOption.builder()
                .option(savedMenu.getName())
                .price(createMenuRequest.singlePrice())
                .menu(menu)
                .build();
            savedMenu.getMenuOptions().add(menuOption);
        } else {
            for (var option : createMenuRequest.optionPrices()) {
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
    public void createMenuCategory(Integer shopId, Integer ownerId, CreateCategoryRequest createCategoryRequest) {
        Shop shop = ownerUtiltService.getOwnerShopById(shopId, ownerId);
        MenuCategory menuCategory = MenuCategory.builder()
            .shop(shop)
            .name(createCategoryRequest.name())
            .build();
        menuCategoryRepository.save(menuCategory);
    }

    @Transactional
    public void modifyMenu(Integer ownerId, Integer menuId, ModifyMenuRequest modifyMenuRequest) {
        Menu menu = menuRepository.getById(menuId);
        ownerUtiltService.getOwnerShopById(menu.getShop().getId(), ownerId);
        menu.modifyMenu(
            modifyMenuRequest.name(),
            modifyMenuRequest.description()
        );
        menu.modifyMenuImages(modifyMenuRequest.imageUrls(), entityManager);
        menu.modifyMenuCategories(menuCategoryRepository.findAllByIdIn(modifyMenuRequest.categoryIds()), entityManager);
        if (modifyMenuRequest.isSingle()) {
            menu.modifyMenuSingleOptions(modifyMenuRequest, entityManager);
        } else {
            menu.modifyMenuMultipleOptions(modifyMenuRequest.optionPrices(), entityManager);
        }
    }

    @Transactional
    public void modifyCategory(Integer ownerId, Integer categoryId, ModifyCategoryRequest modifyCategoryRequest) {
        MenuCategory menuCategory = menuCategoryRepository.getById(categoryId);
        ownerUtiltService.getOwnerShopById(menuCategory.getShop().getId(), ownerId);
        menuCategory.modifyName(modifyCategoryRequest.name());
    }
}
