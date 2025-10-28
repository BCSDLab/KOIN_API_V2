package in.koreatech.koin.domain.shoptoOrderable.repository;

import in.koreatech.koin.domain.shoptoOrderable.model.ShopToOrderable;
import in.koreatech.koin.domain.shoptoOrderable.model.ShopToOrderableRequestStatus;

import org.springframework.data.repository.Repository;

public interface ShopToOrderableRepository extends Repository<ShopToOrderable, Integer> {

    ShopToOrderable save(ShopToOrderable shopToOrderable);

    boolean existsByShopId(Integer shopId);

    boolean existsByShopIdAndRequestStatus(Integer shopId, ShopToOrderableRequestStatus requestStatus);
}
