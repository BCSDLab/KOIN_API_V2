package in.koreatech.koin.domain.order.cart.service.implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderableShopGetter {

    private final OrderableShopRepository orderableShopRepository;

    public OrderableShop getOrderableShop(Integer orderableShopId) {
        return orderableShopRepository.getById(orderableShopId);
    }
}
