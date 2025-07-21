package in.koreatech.koin.domain.order.shop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.order.shop.dto.event.OrderableShopEventsResponse;
import in.koreatech.koin.domain.order.shop.service.OrderableShopEventService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderableShopEventController implements OrderableShopEventApi {

    private final OrderableShopEventService orderableShopEventService;

    @GetMapping("/order/shop/{orderableShopId}/events")
    public ResponseEntity<OrderableShopEventsResponse> getShopEvents(
        @PathVariable Integer orderableShopId
    ) {
        var response = orderableShopEventService.getOrderableShopEvents(orderableShopId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/shops/events")
    public ResponseEntity<OrderableShopEventsResponse> getShopAllEvent() {
        var response = orderableShopEventService.getAllOrderableShopEvents();
        return ResponseEntity.ok(response);
    }
}
