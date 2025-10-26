package in.koreatech.koin.domain.shoptoOrderable.repository;

import in.koreatech.koin.domain.shoptoOrderable.model.ShopToOrderable;
import in.koreatech.koin.domain.shoptoOrderable.model.ShopToOrderableRequestStatus;

import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface ShopToOrderableRepository extends Repository<ShopToOrderable, Integer> {

    ShopToOrderable save(ShopToOrderable shopToOrderable);

    Optional<ShopToOrderable> findByShopId(Integer shopId);

    boolean existsByShopId(Integer shopId);

    boolean existsByShopIdAndRequestStatus(Integer shopId, ShopToOrderableRequestStatus requestStatus);
}
