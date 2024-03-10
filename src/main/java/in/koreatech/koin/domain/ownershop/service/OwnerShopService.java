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
            .build();
        shopRepository.save(newShop);
    }
}
