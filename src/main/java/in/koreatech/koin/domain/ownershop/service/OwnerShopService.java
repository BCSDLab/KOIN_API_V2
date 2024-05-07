package in.koreatech.koin.domain.ownershop.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.ownershop.EventArticleCreateShopEvent;
import in.koreatech.koin.domain.ownershop.dto.CreateEventRequest;
import in.koreatech.koin.domain.ownershop.dto.ModifyEventRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopEventsResponse;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse.InnerShopResponse;
import in.koreatech.koin.domain.shop.dto.CreateCategoryRequest;
import in.koreatech.koin.domain.shop.dto.CreateMenuRequest;
import in.koreatech.koin.domain.shop.dto.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.ModifyCategoryRequest;
import in.koreatech.koin.domain.shop.dto.ModifyMenuRequest;
import in.koreatech.koin.domain.shop.dto.ModifyShopRequest;
import in.koreatech.koin.domain.shop.dto.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.ShopResponse;
import in.koreatech.koin.domain.shop.model.EventArticle;
import in.koreatech.koin.domain.shop.model.EventArticleImage;
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
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerShopService {

    private final EntityManager entityManager;
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
    private final ApplicationEventPublisher eventPublisher;

    public OwnerShopsResponse getOwnerShops(Integer ownerId) {
        List<Shop> shops = shopRepository.findAllByOwnerId(ownerId);
        var innerShopResponses = shops.stream().map(shop -> {
                boolean eventDuration = eventArticleRepository.isDurationEvent(shop.getId(), LocalDate.now(clock));
                return InnerShopResponse.from(shop, eventDuration);
            })
            .toList();
        return OwnerShopsResponse.from(innerShopResponses);
    }

    @Transactional
    public void createOwnerShops(Integer ownerId, OwnerShopsRequest ownerShopsRequest) {
        Owner owner = ownerRepository.getById(ownerId);
        Shop newShop = ownerShopsRequest.toEntity(owner);
        Shop savedShop = shopRepository.save(newShop);
        List<String> categoryNames = List.of("추천 메뉴", "메인 메뉴", "세트 메뉴", "사이드 메뉴");
        for (String categoryName : categoryNames) {
            MenuCategory menuCategory = MenuCategory.builder()
                .shop(savedShop)
                .name(categoryName)
                .build();
            menuCategoryRepository.save(menuCategory);
        }
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

    public ShopResponse getShopByShopId(Integer ownerId, Integer shopId) {
        Shop shop = getOwnerShopById(shopId, ownerId);
        boolean eventDuration = eventArticleRepository.isDurationEvent(shopId, LocalDate.now(clock));
        return ShopResponse.from(shop, eventDuration);
    }

    private Shop getOwnerShopById(Integer shopId, Integer ownerId) {
        Shop shop = shopRepository.getById(shopId);
        if (!Objects.equals(shop.getOwner().getId(), ownerId)) {
            throw AuthorizationException.withDetail("ownerId: " + ownerId);
        }
        return shop;
    }

    public MenuDetailResponse getMenuByMenuId(Integer ownerId, Integer menuId) {
        Menu menu = menuRepository.getById(menuId);
        getOwnerShopById(menu.getShopId(), ownerId);
        List<MenuCategory> menuCategories = menu.getMenuCategoryMaps()
            .stream()
            .map(MenuCategoryMap::getMenuCategory)
            .toList();
        return MenuDetailResponse.createMenuDetailResponse(menu, menuCategories);
    }

    public ShopMenuResponse getMenus(Integer shopId, Integer ownerId) {
        Shop shop = getOwnerShopById(shopId, ownerId);
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByShopId(shop.getId());
        Collections.sort(menuCategories);
        return ShopMenuResponse.from(menuCategories);
    }

    public MenuCategoriesResponse getCategories(Integer shopId, Integer ownerId) {
        Shop shop = getOwnerShopById(shopId, ownerId);
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByShopId(shop.getId());
        return MenuCategoriesResponse.from(menuCategories);
    }

    @Transactional
    public void deleteMenuByMenuId(Integer ownerId, Integer menuId) {
        Menu menu = menuRepository.getById(menuId);
        getOwnerShopById(menu.getShopId(), ownerId);
        menuRepository.deleteById(menuId);
    }

    @Transactional
    public void deleteCategory(Integer ownerId, Integer categoryId) {
        MenuCategory menuCategory = menuCategoryRepository.getById(categoryId);
        getOwnerShopById(menuCategory.getShop().getId(), ownerId);
        menuCategoryRepository.deleteById(categoryId);
    }

    @Transactional
    public void createMenu(Integer shopId, Integer ownerId, CreateMenuRequest createMenuRequest) {
        getOwnerShopById(shopId, ownerId);
        Menu menu = createMenuRequest.toEntity(shopId);
        Menu savedMenu = menuRepository.save(menu);
        for (Integer categoryId : createMenuRequest.categoryIds()) {
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
    public void createMenuCategory(Integer shopId, Integer ownerId, CreateCategoryRequest createCategoryRequest) {
        Shop shop = getOwnerShopById(shopId, ownerId);
        MenuCategory menuCategory = MenuCategory.builder()
            .shop(shop)
            .name(createCategoryRequest.name())
            .build();
        menuCategoryRepository.save(menuCategory);
    }

    @Transactional
    public void modifyMenu(Integer ownerId, Integer menuId, ModifyMenuRequest modifyMenuRequest) {
        Menu menu = menuRepository.getById(menuId);
        getOwnerShopById(menu.getShopId(), ownerId);
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
        getOwnerShopById(menuCategory.getShop().getId(), ownerId);
        menuCategory.modifyName(modifyCategoryRequest.name());
    }

    @Transactional
    public void modifyShop(Integer ownerId, Integer shopId, ModifyShopRequest modifyShopRequest) {
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
        shop.modifyShopImages(modifyShopRequest.imageUrls(), entityManager);
        shop.modifyShopOpens(modifyShopRequest.open(), entityManager);
        shop.modifyShopCategories(shopCategoryRepository.findAllByIdIn(modifyShopRequest.categoryIds()), entityManager);
        shopRepository.save(shop);
    }

    @Transactional
    public void createEvent(Integer ownerId, Integer shopId, CreateEventRequest shopEventRequest) {
        Shop shop = getOwnerShopById(shopId, ownerId);
        EventArticle eventArticle = EventArticle.builder()
            .shop(shop)
            .startDate(shopEventRequest.startDate())
            .endDate(shopEventRequest.endDate())
            .title(shopEventRequest.title())
            .content(shopEventRequest.content())
            .user(shop.getOwner().getUser())
            .hit(0)
            .ip("")
            .build();
        EventArticle savedEventArticle = eventArticleRepository.save(eventArticle);
        for (String image : shopEventRequest.thumbnailImages()) {
            savedEventArticle.getThumbnailImages()
                .add(EventArticleImage.builder()
                    .eventArticle(eventArticle)
                    .thumbnailImage(image)
                    .build());
        }
        List<EventArticleImage> eventArticleImages = savedEventArticle.getThumbnailImages();
        eventPublisher.publishEvent(
            new EventArticleCreateShopEvent(
                shop.getId(),
                shop.getName(),
                savedEventArticle.getTitle(),
                eventArticleImages.isEmpty() ? null : eventArticleImages.get(0).getThumbnailImage()
            )
        );
    }

    @Transactional
    public void modifyEvent(Integer ownerId, Integer shopId, Integer eventId, ModifyEventRequest modifyEventRequest) {
        getOwnerShopById(shopId, ownerId);
        EventArticle eventArticle = eventArticleRepository.getById(eventId);
        eventArticle.modifyArticle(
            modifyEventRequest.title(),
            modifyEventRequest.content(),
            modifyEventRequest.thumbnailImages(),
            modifyEventRequest.startDate(),
            modifyEventRequest.endDate(),
            entityManager
        );
    }

    @Transactional
    public void deleteEvent(Integer ownerId, Integer shopId, Integer eventId) {
        getOwnerShopById(shopId, ownerId);
        eventArticleRepository.deleteById(eventId);
    }

    public OwnerShopEventsResponse getShopEvent(Integer shopId, Integer ownerId) {
        Shop shop = getOwnerShopById(shopId, ownerId);
        return OwnerShopEventsResponse.from(shop);
    }
}
