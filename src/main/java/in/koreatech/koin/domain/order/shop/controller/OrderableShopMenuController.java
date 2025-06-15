package in.koreatech.koin.domain.order.shop.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.order.shop.dto.menu.OrderableShopMenuResponse;
import in.koreatech.koin.domain.order.shop.dto.menu.OrderableShopMenusResponse;
import in.koreatech.koin.domain.order.shop.service.OrderableShopMenuQueryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderableShopMenuController implements OrderableShopMenuApi {

    private final OrderableShopMenuQueryService orderableShopMenuQueryService;

    @GetMapping("/order/shop/{orderableShopId}/menus")
    public ResponseEntity<List<OrderableShopMenusResponse>> getOrderableShopMenus(
        @PathVariable Integer orderableShopId
    ) {
        List<OrderableShopMenusResponse> orderableShopMenusResponses = orderableShopMenuQueryService.getOrderableShopMenus(
            orderableShopId);
        return ResponseEntity.ok(orderableShopMenusResponses);
    }

    @GetMapping("/order/shop/{orderableShopId}/menus/{orderableShopMenuId}")
    public ResponseEntity<OrderableShopMenuResponse> getOrderableShopMenu(
        @PathVariable Integer orderableShopId,
        @PathVariable Integer orderableShopMenuId
    ) {
        OrderableShopMenuResponse orderableShopMenuResponse = orderableShopMenuQueryService.getOrderableShopMenu(
            orderableShopMenuId);
        return ResponseEntity.ok(orderableShopMenuResponse);
    }
}
