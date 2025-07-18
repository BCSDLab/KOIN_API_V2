package in.koreatech.koin.domain.order.shop.service;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.shop.dto.event.OrderableShopEventsResponse;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderableShopEventService {

    private final OrderableShopRepository orderableShopRepository;
    private final Clock clock;

    public OrderableShopEventsResponse getOrderableShopEvents(Integer orderableShopId) {
        OrderableShop orderableShop = orderableShopRepository.getByIdWithShopEvent(orderableShopId);

        return OrderableShopEventsResponse.of(orderableShop, clock);
    }

    public OrderableShopEventsResponse getAllOrderableShopEvents() {
        List<OrderableShop> orderableShops = orderableShopRepository.findAllWithShopEvent();
        return OrderableShopEventsResponse.of(orderableShops, clock);
    }
}
