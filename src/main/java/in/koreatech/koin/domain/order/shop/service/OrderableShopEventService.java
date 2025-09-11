package in.koreatech.koin.domain.order.shop.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.shop.dto.event.OrderableShopEventsResponse;
import in.koreatech.koin.domain.order.shop.model.readmodel.OrderableShopEvent;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopEventQueryRepository;
import in.koreatech.koin.domain.shop.model.event.EventArticleImage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderableShopEventService {

    private final OrderableShopEventQueryRepository orderableShopEventQueryRepository;

    public OrderableShopEventsResponse getOrderableShopEvents(Integer orderableShopId) {
        List<OrderableShopEvent> orderableShopEvents =
            orderableShopEventQueryRepository.getOngoingEventById(orderableShopId);

        if (orderableShopEvents.isEmpty()) {
            return new OrderableShopEventsResponse(Collections.emptyList());
        }

        Map<Integer, List<EventArticleImage>> eventImageMap =
            getEventArticleImagesMap(orderableShopEvents);

        return OrderableShopEventsResponse.of(orderableShopEvents, eventImageMap);
    }

    public OrderableShopEventsResponse getAllOrderableShopEvents() {
        List<OrderableShopEvent> allOrderableShopEvents =  orderableShopEventQueryRepository.getAllOngoingEvents();

        Map<Integer, List<EventArticleImage>> eventImageMap =
            getEventArticleImagesMap(allOrderableShopEvents);

        return OrderableShopEventsResponse.of(allOrderableShopEvents, eventImageMap);
    }

    private List<Integer> getEventIds(List<OrderableShopEvent> orderableShopEvents) {
        return orderableShopEvents.stream()
            .map(OrderableShopEvent::eventId)
            .toList();
    }

    private Map<Integer, List<EventArticleImage>> getEventArticleImagesMap(List<OrderableShopEvent> orderableShopEvents) {
        return orderableShopEventQueryRepository.getEventArticleImagesMap(getEventIds(orderableShopEvents));
    }
}
