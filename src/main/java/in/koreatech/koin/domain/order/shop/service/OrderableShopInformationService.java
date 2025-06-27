package in.koreatech.koin.domain.order.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.shop.dto.shopinfo.OrderableShopDeliveryResponse;
import in.koreatech.koin.domain.order.shop.dto.shopinfo.OrderableShopInfoDetailResponse;
import in.koreatech.koin.domain.order.shop.dto.shopinfo.OrderableShopInfoSummaryResponse;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderableShopInformationService {

    private final OrderableShopRepository orderableShopRepository;

    public OrderableShopInfoSummaryResponse getOrderableShopInfoSummaryById(Integer orderableShopId) {
        return OrderableShopInfoSummaryResponse.from(
            orderableShopRepository.getOrderableShopInfoSummaryById(orderableShopId));
    }

    public OrderableShopInfoDetailResponse getOrderableShopInfoDetailById(Integer orderableShopId) {
        return OrderableShopInfoDetailResponse.from(
            orderableShopRepository.getOrderableShopInfoDetailById(orderableShopId));
    }

    public OrderableShopDeliveryResponse getOrderableShopDeliveryResponse(Integer orderableShopId) {
        OrderableShop orderableShop = orderableShopRepository.getById(orderableShopId);
        return OrderableShopDeliveryResponse.from(orderableShop);
    }
}
