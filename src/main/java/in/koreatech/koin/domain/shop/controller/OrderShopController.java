package in.koreatech.koin.domain.shop.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.shop.dto.order.OrderableShopsFilterCriteria;
import in.koreatech.koin.domain.shop.dto.order.OrderableShopsResponse;
import in.koreatech.koin.domain.shop.dto.order.OrderableShopsSortCriteria;
import in.koreatech.koin.domain.shop.service.OrderableShopService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderShopController implements OrderShopApi{

    private final OrderableShopService orderableShopService;

    @GetMapping("/order/shops")
    public ResponseEntity<List<OrderableShopsResponse>> getOrderableShops(
        @RequestParam(name = "sorter", defaultValue = "NONE") OrderableShopsSortCriteria sortBy,
        @RequestParam(name = "filter", required = false) List<OrderableShopsFilterCriteria> orderableShopsSortCriteria,
        @RequestParam(name = "minimum_order_amount", required = false) Integer minimumOrderAmount
    ) {
        var orderableShops = orderableShopService.getOrderableShops(sortBy, orderableShopsSortCriteria, minimumOrderAmount);
        return ResponseEntity.ok(orderableShops);
    }
}
