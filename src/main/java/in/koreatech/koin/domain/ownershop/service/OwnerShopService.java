package in.koreatech.koin.domain.ownershop.service;

import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.owner.domain.Owner;
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
import jakarta.persistence.EntityNotFoundException;
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
        LocalTime openTime = ownerShopsRequest.open().openTime();
        LocalTime closeTime = ownerShopsRequest.open().closeTime();
        if(closeTime.isBefore(openTime)) throw new IllegalArgumentException();

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
        shopRepository.save(newShop);

        ownerShopsRequest.imageUrls().stream().forEach(imageUrl -> {
            ShopImage shopImage = ShopImage.builder()
                .shop(newShop)
                .imageUrl(imageUrl)
                .build();
            shopImageRepository.save(shopImage);
        });

        ShopOpen shopOpen = ShopOpen.builder()
            .shop(newShop)
            .openTime(ownerShopsRequest.open().openTime())
            .closeTime(ownerShopsRequest.open().closeTime())
            .dayOfWeek(ownerShopsRequest.open().dayOfWeek())
            .closed(ownerShopsRequest.open().closed())
            .build();
        shopOpenRepository.save(shopOpen);

        ownerShopsRequest.categoryIds().stream().forEach(categoryId -> {
            ShopCategory shopCategory = shopCategoryRepository.getById(categoryId);
            ShopCategoryMap shopCategoryMap = ShopCategoryMap.builder()
                .shopCategory(shopCategory)
                .shop(newShop)
                .build();
            shopCategoryMapRepository.save(shopCategoryMap);
        });
    }
}
