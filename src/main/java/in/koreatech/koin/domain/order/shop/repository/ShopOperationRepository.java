package in.koreatech.koin.domain.order.shop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.order.shop.model.entity.shop.ShopOperation;

public interface ShopOperationRepository extends Repository<ShopOperation, Integer> {

    ShopOperation save(ShopOperation shopOperation);

    @Query(value = "SELECT * FROM shop_operation WHERE shop_id = :shopId", nativeQuery = true)
    Optional<ShopOperation> findByShopIdIncludingDeleted(@Param("shopId") Integer shopId);
}
