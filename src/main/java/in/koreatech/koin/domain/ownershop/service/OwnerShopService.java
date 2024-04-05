package in.koreatech.koin.domain.ownershop.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse;
import in.koreatech.koin.domain.shop.dto.CreateCategoryRequest;
import in.koreatech.koin.domain.shop.dto.CreateMenuRequest;
import in.koreatech.koin.domain.shop.dto.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.ModifyCategoryRequest;
import in.koreatech.koin.domain.shop.dto.ModifyMenuRequest;
import in.koreatech.koin.domain.shop.dto.ModifyShopRequest;
import in.koreatech.koin.domain.shop.dto.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.ShopResponse;
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
import in.koreatech.koin.domain.shop.repository.EventArticleRepository;
import in.koreatech.koin.domain.shop.repository.MenuCategoryMapRepository;
import in.koreatech.koin.domain.shop.repository.MenuCategoryRepository;
import in.koreatech.koin.domain.shop.repository.MenuDetailRepository;
import in.koreatech.koin.domain.shop.repository.MenuImageRepository;
import in.koreatech.koin.domain.shop.repository.MenuRepository;
import in.koreatech.koin.domain.shop.repository.ShopCategoryMapRepository;
import in.koreatech.koin.domain.shop.repository.ShopCategoryRepository;
import in.koreatech.koin.domain.shop.repository.ShopImageRepository;
import in.koreatech.koin.domain.shop.repository.ShopOpenRepository;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerShopService {

    private final Clock clock;
    private final ShopRepository shopRepository;
    private final OwnerRepository ownerRepository;
    private final ShopOpenRepository shopOpenRepository;
    private final ShopCategoryMapRepository shopCategoryMapRepository;
    private final ShopCategoryRepository shopCategoryRepository;
    private final ShopImageRepository shopImageRepository;
    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuCategoryMapRepository menuCategoryMapRepository;
    private final MenuImageRepository menuImageRepository;
    private final MenuDetailRepository menuDetailRepository;
    private final EventArticleRepository eventArticleRepository;

    public OwnerShopsResponse getOwnerShops(Long ownerId) {
        List<Shop> shops = shopRepository.findAllByOwnerId(ownerId);
        return OwnerShopsResponse.from(shops);
    }

    @Transactional
    public void createOwnerShops(Long ownerId, OwnerShopsRequest ownerShopsRequest) {
        Owner owner = ownerRepository.getById(ownerId);
        Shop newShop = ownerShopsRequest.toEntity(owner);
        Shop savedShop = shopRepository.save(newShop);

        for (String imageUrl : ownerShopsRequest.imageUrls()) {
            ShopImage shopImage = ShopImage.builder()
                .shop(savedShop)
                .imageUrl(imageUrl)
                .build();
            shopImageRepository.save(shopImage);
        }
        for (OwnerShopsRequest.InnerOpenRequest open : ownerShopsRequest.open()) {
            ShopOpen shopOpen = ShopOpen.builder()
                .shop(savedShop)
                .openTime(open.openTime())
                .closeTime(open.closeTime())
                .dayOfWeek(open.dayOfWeek())
                .closed(open.closed())
                .build();
            shopOpenRepository.save(shopOpen);
        }
        List<ShopCategory> shopCategories = shopCategoryRepository.findAllByIdIn(ownerShopsRequest.categoryIds());
        for (ShopCategory shopCategory : shopCategories) {
            ShopCategoryMap shopCategoryMap = ShopCategoryMap.builder()
                .shopCategory(shopCategory)
                .shop(savedShop)
                .build();
            shopCategoryMapRepository.save(shopCategoryMap);
        }
    }

    public ShopResponse getShopByShopId(Long ownerId, Long shopId) {
        Shop shop = getOwnerShopById(shopId, ownerId);
        Boolean eventDuration = eventArticleRepository.isEvent(shopId, LocalDate.now(clock));
        return ShopResponse.from(shop, eventDuration);
    }

    private Shop getOwnerShopById(Long shopId, Long ownerId) {
        Shop shop = shopRepository.getById(shopId);
        if (!Objects.equals(shop.getOwner().getId(), ownerId)) {
            throw AuthorizationException.withDetail("ownerId: " + ownerId);
        }
        return shop;
    }

    public MenuDetailResponse getMenuByMenuId(Long ownerId, Long menuId) {
        Menu menu = menuRepository.getById(menuId);
        getOwnerShopById(menu.getShopId(), ownerId);
        List<MenuCategory> menuCategories = menu.getMenuCategoryMaps()
            .stream()
            .map(MenuCategoryMap::getMenuCategory)
            .toList();
        return MenuDetailResponse.createMenuDetailResponse(menu, menuCategories);
    }

    public ShopMenuResponse getMenus(Long shopId, Long ownerId) {
        Shop shop = getOwnerShopById(shopId, ownerId);
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByShopId(shop.getId());
        return ShopMenuResponse.from(menuCategories);
    }

    public MenuCategoriesResponse getCategories(Long shopId, Long ownerId) {
        Shop shop = getOwnerShopById(shopId, ownerId);
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByShopId(shop.getId());
        return MenuCategoriesResponse.from(menuCategories);
    }

    @Transactional
    public void deleteMenuByMenuId(Long ownerId, Long menuId) {
        Menu menu = menuRepository.getById(menuId);
        getOwnerShopById(menu.getShopId(), ownerId);
        menuRepository.deleteById(menuId);
    }

    @Transactional
    public void deleteCategory(Long ownerId, Long categoryId) {
        MenuCategory menuCategory = menuCategoryRepository.getById(categoryId);
        getOwnerShopById(menuCategory.getShop().getId(), ownerId);
        menuCategoryRepository.deleteById(categoryId);
    }

    @Transactional
    public void createMenu(Long shopId, Long ownerId, CreateMenuRequest createMenuRequest) {
        getOwnerShopById(shopId, ownerId);
        Menu menu = createMenuRequest.toEntity(shopId);
        Menu savedMenu = menuRepository.save(menu);
        for (Long categoryId : createMenuRequest.categoryIds()) {
            MenuCategory menuCategory = menuCategoryRepository.getById(categoryId);
            MenuCategoryMap menuCategoryMap = MenuCategoryMap.builder()
                .menuCategory(menuCategory)
                .menu(savedMenu)
                .build();
            menuCategoryMapRepository.save(menuCategoryMap);
        }
        for (String imageUrl : createMenuRequest.imageUrls()) {
            MenuImage menuImage = MenuImage.builder()
                .imageUrl(imageUrl)
                .menu(savedMenu)
                .build();
            menuImageRepository.save(menuImage);
        }
        if (createMenuRequest.optionPrices() == null) {
            MenuOption menuOption = MenuOption.builder()
                .option(savedMenu.getName())
                .price(createMenuRequest.singlePrice())
                .menu(menu)
                .build();
            menuDetailRepository.save(menuOption);
        } else {
            for (var option : createMenuRequest.optionPrices()) {
                MenuOption menuOption = MenuOption.builder()
                    .option(option.option())
                    .price(option.price())
                    .menu(menu)
                    .build();
                menuDetailRepository.save(menuOption);
            }
        }
    }

    @Transactional
    public void createMenuCategory(Long shopId, Long ownerId, CreateCategoryRequest createCategoryRequest) {
        Shop shop = getOwnerShopById(shopId, ownerId);
        MenuCategory menuCategory = MenuCategory.builder()
            .shop(shop)
            .name(createCategoryRequest.name())
            .build();
        menuCategoryRepository.save(menuCategory);
    }

    @Transactional
    public void modifyMenu(Long ownerId, Long menuId, ModifyMenuRequest modifyMenuRequest) {
        Menu menu = menuRepository.getById(menuId);
        getOwnerShopById(menu.getShopId(), ownerId);
        menu.modifyMenu(
            modifyMenuRequest.name(),
            modifyMenuRequest.description()
        );
        menu.modifyMenuImages(modifyMenuRequest.imageUrls());
        menu.modifyMenuCategories(menuCategoryRepository.findAllByIdIn(modifyMenuRequest.categoryIds()));
        if (modifyMenuRequest.isSingle()) {
            menu.modifyMenuSingleOptions(modifyMenuRequest);
        } else {
            menu.modifyMenuMultieOptions(modifyMenuRequest.optionPrices());
        }
    }

    @Transactional
    public void modifyCategory(Long ownerId, Long categoryId, ModifyCategoryRequest modifyCategoryRequest) {
        MenuCategory menuCategory = menuCategoryRepository.getById(categoryId);
        getOwnerShopById(menuCategory.getShop().getId(), ownerId);
        menuCategory.modifyCategory(modifyCategoryRequest);
    }

    @Transactional
    public void modifyShop(Long ownerId, Long shopId, ModifyShopRequest modifyShopRequest) {
        Shop shop = getOwnerShopById(shopId, ownerId);
        shop.modifyShop(
            modifyShopRequest.name(),
            modifyShopRequest.phone(),
            modifyShopRequest.address(),
            modifyShopRequest.description(),
            modifyShopRequest.delivery(),
            modifyShopRequest.deliveryPrice(),
            modifyShopRequest.payCard(),
            modifyShopRequest.payBank()
        );
        shop.modifyShopImages(modifyShopRequest.imageUrls());
        shop.modifyShopOpens(modifyShopRequest.open());
        shop.modifyShopCategories(shopCategoryRepository.findAllByIdIn(modifyShopRequest.categoryIds()));
    }
}
