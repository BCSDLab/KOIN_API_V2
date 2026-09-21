package in.koreatech.koin.domain.order.order.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.order.order.dto.request.OwnerOrderStatusCriteria;
import in.koreatech.koin.domain.order.order.dto.response.OwnerOrderCountsResponse;
import in.koreatech.koin.domain.order.order.dto.response.OwnerOrderResponse;
import in.koreatech.koin.domain.order.order.dto.response.OwnerOrdersResponse;
import in.koreatech.koin.domain.order.order.service.OwnerOrderService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OwnerOrderController implements OwnerOrderApi {

    private final OwnerOrderService ownerOrderService;

    @GetMapping("/owner/shops/{orderableShopId}/orders")
    public ResponseEntity<OwnerOrdersResponse> getOrders(
        @PathVariable Integer orderableShopId,
        @RequestParam(name = "status") OwnerOrderStatusCriteria status,
        @Auth(permit = {OWNER}) Integer ownerId
    ) {
        OwnerOrdersResponse response = ownerOrderService.getOrders(ownerId, orderableShopId, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/owner/shops/{orderableShopId}/orders/counts")
    public ResponseEntity<OwnerOrderCountsResponse> getOrderCounts(
        @PathVariable Integer orderableShopId,
        @Auth(permit = {OWNER}) Integer ownerId
    ) {
        OwnerOrderCountsResponse response = ownerOrderService.getOrderCounts(ownerId, orderableShopId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/owner/shops/{orderableShopId}/orders/{orderId}")
    public ResponseEntity<OwnerOrderResponse> getOrder(
        @PathVariable Integer orderableShopId,
        @PathVariable Integer orderId,
        @Auth(permit = {OWNER}) Integer ownerId
    ) {
        OwnerOrderResponse response = ownerOrderService.getOrder(ownerId, orderableShopId, orderId);
        return ResponseEntity.ok(response);
    }
}
