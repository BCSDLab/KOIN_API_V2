package in.koreatech.koin.domain.order.shop.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.order.shop.dto.shopinfo.OrderableShopDeliveryResponse;
import in.koreatech.koin.domain.order.shop.dto.shopinfo.OrderableShopInfoDetailResponse;
import in.koreatech.koin.domain.order.shop.dto.shopinfo.OrderableShopInfoSummaryResponse;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsFilterCriteria;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsResponse;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsSortCriteria;
import in.koreatech.koin.domain.order.shop.service.OrderableShopInformationService;
import in.koreatech.koin.domain.order.shop.service.OrderableShopListService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderableShopController implements OrderableShopApi {

    private final OrderableShopListService orderableShopListService;
    private final OrderableShopInformationService orderableShopInformationService;

    @GetMapping("/order/shops")
    public ResponseEntity<List<OrderableShopsResponse>> getOrderableShops(
        @RequestParam(name = "sorter", defaultValue = "NONE") OrderableShopsSortCriteria sortBy,
        @RequestParam(name = "filter", required = false) List<OrderableShopsFilterCriteria> orderableShopsSortCriteria,
        @RequestParam(name = "minimum_order_amount", required = false) Integer minimumOrderAmount
    ) {
        List<OrderableShopsResponse> orderableShops = orderableShopListService.getOrderableShops(sortBy,
            orderableShopsSortCriteria, minimumOrderAmount);
        return ResponseEntity.ok(orderableShops);
    }

    @GetMapping("/order/shop/{orderableShopId}/summary")
    public ResponseEntity<OrderableShopInfoSummaryResponse> getOrderableShopInfoSummary(
        @PathVariable Integer orderableShopId
    ) {
        OrderableShopInfoSummaryResponse orderableShopInfoSummary = orderableShopInformationService.getOrderableShopInfoSummaryById(
            orderableShopId);
        return ResponseEntity.ok(orderableShopInfoSummary);
    }

    @GetMapping("/order/shop/{orderableShopId}/detail")
    public ResponseEntity<OrderableShopInfoDetailResponse> getOrderableShopInfoDetail(
        @PathVariable Integer orderableShopId
    ) {
        OrderableShopInfoDetailResponse orderableShopInfoSummary = orderableShopInformationService.getOrderableShopInfoDetailById(
            orderableShopId);
        return ResponseEntity.ok(orderableShopInfoSummary);
    }

    @GetMapping("/order/shop/{orderableShopId}/delivery")
    public ResponseEntity<OrderableShopDeliveryResponse> getOrderableShopDeliveryResponse(
        @PathVariable Integer orderableShopId
    ) {
        OrderableShopDeliveryResponse response = orderableShopInformationService.getOrderableShopDeliveryResponse(
            orderableShopId);
        return ResponseEntity.ok(response);
    }
}
