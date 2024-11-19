package in.koreatech.koin.domain.ownershop.service;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerShopUtilService {

    private final ShopRepository shopRepository;

    public Shop getOwnerShopById(Integer shopId, Integer ownerId) {
        Shop shop = shopRepository.getById(shopId);
        if (!Objects.equals(shop.getOwner().getId(), ownerId)) {
            throw AuthorizationException.withDetail("ownerId: " + ownerId);
        }
        return shop;
    }
}
