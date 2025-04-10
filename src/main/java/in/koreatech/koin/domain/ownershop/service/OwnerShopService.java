package in.koreatech.koin.domain.ownershop.service;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse.InnerShopResponse;
import in.koreatech.koin.domain.shop.cache.aop.RefreshShopsCache;
import in.koreatech.koin.domain.shop.dto.shop.request.ModifyShopRequest;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopResponse;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.repository.event.EventArticleRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopCategoryRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

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
    private final OwnerShopUtilService ownerShopUtilService;

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
    @RefreshShopsCache
    public void createOwnerShops(Integer ownerId, OwnerShopsRequest ownerShopsRequest) {
        Owner owner = ownerRepository.getById(ownerId);
        ShopCategory shopMainCategory = shopCategoryRepository.getById(ownerShopsRequest.mainCategoryId());
        Shop savedShop = shopRepository.save(ownerShopsRequest.toEntity(owner, shopMainCategory));
        List<ShopCategory> shopCategories = shopCategoryRepository.findAllByIdIn(ownerShopsRequest.categoryIds());
        savedShop.addDefaultMenuCategory();
        savedShop.addShopImages(ownerShopsRequest.imageUrls());
        savedShop.addOpens(ownerShopsRequest.toShopOpens(savedShop));
        savedShop.addShopCategories(shopCategories);
    }

    public ShopResponse getShopByShopId(Integer ownerId, Integer shopId) {
        Shop shop = ownerShopUtilService.getOwnerShopById(shopId, ownerId);
        return ShopResponse.from(shop, LocalDate.now(clock));
    }

    @Transactional
    @RefreshShopsCache
    public void modifyShop(Integer ownerId, Integer shopId, ModifyShopRequest modifyShopRequest) {
        Shop shop = ownerShopUtilService.getOwnerShopById(shopId, ownerId);
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
        shop.modifyShopOpens(modifyShopRequest.toShopOpens(shop), entityManager);
        shop.modifyShopCategories(shopCategoryRepository.findAllByIdIn(modifyShopRequest.categoryIds()), entityManager);
        shopRepository.save(shop);
    }
}
