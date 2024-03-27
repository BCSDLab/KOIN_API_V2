package in.koreatech.koin.domain.ownershop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse;
import in.koreatech.koin.domain.shop.dto.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.ShopResponse;
import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.model.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.ShopImage;
import in.koreatech.koin.domain.shop.model.ShopOpen;
import in.koreatech.koin.domain.shop.repository.MenuCategoryRepository;
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

    private final ShopRepository shopRepository;
    private final OwnerRepository ownerRepository;
    private final ShopOpenRepository shopOpenRepository;
    private final ShopCategoryMapRepository shopCategoryMapRepository;
    private final ShopCategoryRepository shopCategoryRepository;
    private final ShopImageRepository shopImageRepository;
    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;

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
        return ShopResponse.from(shop);
    }
    private Shop getOwnerShopById(Long shopId, Long ownerId) {
        Shop shop = shopRepository.getById(shopId);
        if (shop.getOwner().getId() != ownerId) {
            throw AuthorizationException.withDetail("ownerId: " + ownerId);
        }
        return shop;
    }

    public MenuDetailResponse getMenuByMenuId(Long ownerId, Long menuId) {
        Menu menu = menuRepository.getById(menuId);
        Shop shop = getOwnerShopById(menu.getShopId(), ownerId);
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
}
