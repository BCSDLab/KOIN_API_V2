package in.koreatech.koin.domain.ownershop.repository;

import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus;

import org.springframework.data.repository.Repository;

public interface ShopOrderServiceRequestRepository extends Repository<ShopOrderServiceRequest, Integer> {

    ShopOrderServiceRequest save(ShopOrderServiceRequest ShopOrderServiceRequest);

    boolean existsByShopId(Integer shopId);

    boolean existsByShopIdAndRequestStatus(Integer shopId, ShopOrderServiceRequestStatus requestStatus);
}
