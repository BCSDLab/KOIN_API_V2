package in.koreatech.koin.domain.shoptoOrderable.repository;

import in.koreatech.koin.domain.shoptoOrderable.dto.ShopToOrderableResponse;
import in.koreatech.koin.domain.shoptoOrderable.model.ShopToOrderable;

import org.springframework.data.repository.Repository;

public interface ShopToOrderableRepository extends Repository<ShopToOrderable, Integer> {
}
