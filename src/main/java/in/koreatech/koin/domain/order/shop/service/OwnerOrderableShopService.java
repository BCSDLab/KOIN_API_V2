package in.koreatech.koin.domain.order.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerOrderableShopService {

    private final OrderableShopRepository orderableShopRepository;

    @Transactional
    public void changeOpenStatus(Integer ownerId, Integer orderableShopId, boolean isOpen) {
        OrderableShop orderableShop = orderableShopRepository.getById(orderableShopId);
        orderableShop.requireOwner(ownerId);
        orderableShop.changeOpenStatus(isOpen);
    }
}
