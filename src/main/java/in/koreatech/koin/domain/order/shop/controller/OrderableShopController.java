package in.koreatech.koin.domain.order.shop.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsFilterCriteria;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsResponse;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsSortCriteria;
import in.koreatech.koin.domain.order.shop.service.OrderableShopListService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderableShopController implements OrderableShopApi {

    private final OrderableShopListService orderableShopListService;

    @GetMapping("/order/shops")
    public ResponseEntity<List<OrderableShopsResponse>> getOrderableShops(
        @RequestParam(name = "sorter", defaultValue = "NONE") OrderableShopsSortCriteria sortBy,
        @RequestParam(name = "filter", required = false) List<OrderableShopsFilterCriteria> orderableShopsSortCriteria,
        @RequestParam(name = "minimum_order_amount", required = false) Integer minimumOrderAmount
    ) {
        List<OrderableShopsResponse> orderableShops = orderableShopListService.getOrderableShops(sortBy, orderableShopsSortCriteria, minimumOrderAmount);
        return ResponseEntity.ok(orderableShops);
    }
}
