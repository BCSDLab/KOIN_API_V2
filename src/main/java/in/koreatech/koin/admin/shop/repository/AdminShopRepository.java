package in.koreatech.koin.admin.shop.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.ShopNotFoundException;
import in.koreatech.koin.domain.shop.model.Shop;

public interface AdminShopRepository extends Repository<Shop, Integer> {

    Page<Shop> findAllByIsDeleted(boolean isDeleted, Pageable pageable);

    Integer countAllByIsDeleted(boolean isDeleted);

    Shop save(Shop shop);

    Optional<Shop> findById(Integer shopId);

    default Shop getById(Integer shopId) {
        return findById(shopId)
            .orElseThrow(() -> ShopNotFoundException.withDetail("shopId: " + shopId));
    }

    List<Shop> findAllByOwnerId(Integer ownerId);
}
