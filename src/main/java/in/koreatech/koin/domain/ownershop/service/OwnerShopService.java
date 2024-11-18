package in.koreatech.koin.domain.ownershop.service;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse.InnerShopResponse;
import in.koreatech.koin.domain.shop.dto.shop.ModifyShopRequest;
import in.koreatech.koin.domain.shop.dto.shop.ShopResponse;
import in.koreatech.koin.domain.shop.model.menu.MenuCategory;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.model.shop.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.shop.ShopImage;
import in.koreatech.koin.domain.shop.model.shop.ShopOpen;
import in.koreatech.koin.domain.shop.repository.event.EventArticleRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopCategoryRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerShopService {

    private final EntityManager entityManager;
    private final Clock clock;
    private final ShopRepository shopRepository;
    private final OwnerRepository ownerRepository;
    private final ShopCategoryRepository shopCategoryRepository;
    private final EventArticleRepository eventArticleRepository;

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
        ShopCategory shopMainCategory = shopCategoryRepository.getById(ownerShopsRequest.mainCategoryId());
        Shop newShop = ownerShopsRequest.toEntity(owner, shopMainCategory);
        Shop savedShop = shopRepository.save(newShop);
        List<String> categoryNames = List.of("추천 메뉴", "메인 메뉴", "세트 메뉴", "사이드 메뉴");
        for (String categoryName : categoryNames) {
            MenuCategory menuCategory = MenuCategory.builder()
                    .shop(savedShop)
                    .name(categoryName)
                    .build();
            savedShop.getMenuCategories().add(menuCategory);
        }
        for (String imageUrl : ownerShopsRequest.imageUrls()) {
            ShopImage shopImage = ShopImage.builder()
                    .shop(savedShop)
                    .imageUrl(imageUrl)
                    .build();
            savedShop.getShopImages().add(shopImage);
        }
        for (OwnerShopsRequest.InnerOpenRequest open : ownerShopsRequest.open()) {
            ShopOpen shopOpen = ShopOpen.builder()
                    .shop(savedShop)
                    .openTime(open.openTime())
                    .closeTime(open.closeTime())
                    .dayOfWeek(open.dayOfWeek())
                    .closed(open.closed())
                    .build();
            savedShop.getShopOpens().add(shopOpen);
        }
        List<ShopCategory> shopCategories = shopCategoryRepository.findAllByIdIn(ownerShopsRequest.categoryIds());
        for (ShopCategory shopCategory : shopCategories) {
            ShopCategoryMap shopCategoryMap = ShopCategoryMap.builder()
                    .shopCategory(shopCategory)
                    .shop(savedShop)
                    .build();
            savedShop.getShopCategories().add(shopCategoryMap);
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

    @Transactional
    public void modifyShop(Integer ownerId, Integer shopId, ModifyShopRequest modifyShopRequest) {
        Shop shop = getOwnerShopById(shopId, ownerId);
        ShopCategory shopCategory = shopCategoryRepository.getById(modifyShopRequest.mainCategoryId());
        shop.modifyShop(
                modifyShopRequest.name(),
                modifyShopRequest.phone(),
                modifyShopRequest.address(),
                modifyShopRequest.description(),
                modifyShopRequest.delivery(),
                modifyShopRequest.deliveryPrice(),
                modifyShopRequest.payCard(),
                modifyShopRequest.payBank(),
                modifyShopRequest.bank(),
                modifyShopRequest.accountNumber(),
                shopCategory
        );
        shop.modifyShopImages(modifyShopRequest.imageUrls(), entityManager);
        shop.modifyShopOpens(modifyShopRequest.open(), entityManager);
        shop.modifyShopCategories(shopCategoryRepository.findAllByIdIn(modifyShopRequest.categoryIds()), entityManager);
        shopRepository.save(shop);
    }
}
