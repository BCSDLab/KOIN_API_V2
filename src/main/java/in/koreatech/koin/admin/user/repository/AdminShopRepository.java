package in.koreatech.koin.admin.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.ShopNotFoundException;
import in.koreatech.koin.domain.shop.model.Shop;

public interface AdminShopRepository extends Repository<Shop, Integer> {

    List<Shop> findAllByOwnerId(Integer ownerId);

    Optional<Shop> findById(Integer shopId);

    default Shop getById(Integer shopId) {
        return findById(shopId)
            .orElseThrow(() -> ShopNotFoundException.withDetail("shopId: " + shopId));
    }
}
