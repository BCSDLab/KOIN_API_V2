package in.koreatech.koin.domain.ownershop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.owner.domain.Owner;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerShopService {

    private final ShopRepository shopRepository;
    private final OwnerRepository ownerRepository;

    public OwnerShopsResponse getOwnerShops(Long ownerId) {
        List<Shop> shops = shopRepository.findAllByOwnerId(ownerId);
        return OwnerShopsResponse.from(shops);
    }

    @Transactional
    public void createOwnerShops(Long ownerId, OwnerShopsRequest ownerShopsRequest) {
        Owner owner = ownerRepository.getById(ownerId);
        // TODO: shop패키지 model 생성(shop_images, shop_opens, shop_category_map), repository 생성
        // select * from shops;
        // select * from shop_images;
        // select * from shop_opens;
        // select * from shop_category_map;
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
            .chosung(ownerShopsRequest.name().substring(1))
            .isDeleted(false)
            .isEvent(false)
            .remarks("")
            .hit(0L)
            .build();
        shopRepository.save(newShop);
    }
}
