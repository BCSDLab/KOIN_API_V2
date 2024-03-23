package in.koreatech.koin.domain.ownershop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.model.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.ShopImage;
import in.koreatech.koin.domain.shop.model.ShopOpen;
import in.koreatech.koin.domain.shop.repository.ShopCategoryMapRepository;
import in.koreatech.koin.domain.shop.repository.ShopCategoryRepository;
import in.koreatech.koin.domain.shop.repository.ShopImageRepository;
import in.koreatech.koin.domain.shop.repository.ShopOpenRepository;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
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

    public OwnerShopsResponse getOwnerShops(Long ownerId) {
        List<Shop> shops = shopRepository.findAllByOwnerId(ownerId);
        return OwnerShopsResponse.from(shops);
    }

    @Transactional
    public void createOwnerShops(Long ownerId, OwnerShopsRequest ownerShopsRequest) {
        Owner owner = ownerRepository.getById(ownerId);
        Shop newShop = Shop.builder()
            .owner(owner)
            .address(ownerShopsRequest.address())
            .deliveryPrice(ownerShopsRequest.deliveryPrice())
            .delivery(ownerShopsRequest.delivery())
            .description(ownerShopsRequest.description())
            .payBank(ownerShopsRequest.payBank())
            .payCard(ownerShopsRequest.payCard())
            .phone(ownerShopsRequest.phone())
            .name(ownerShopsRequest.name())
            .internalName(ownerShopsRequest.name())
            .chosung(ownerShopsRequest.name().substring(0, 1))
            .isDeleted(false)
            .isEvent(false)
            .remarks("")
            .hit(0L)
            .build();

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
}
