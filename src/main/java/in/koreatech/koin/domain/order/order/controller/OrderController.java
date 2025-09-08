package in.koreatech.koin.domain.order.order.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.order.order.dto.request.OrderSearchCondition;
import in.koreatech.koin.domain.order.order.dto.request.OrderSearchPeriodCriteria;
import in.koreatech.koin.domain.order.order.dto.request.OrderStatusCriteria;
import in.koreatech.koin.domain.order.order.dto.request.OrderTypeCriteria;
import in.koreatech.koin.domain.order.order.dto.response.InprogressOrderResponse;
import in.koreatech.koin.domain.order.order.dto.response.OrdersResponse;
import in.koreatech.koin.domain.order.order.service.OrderService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController implements OrderApi {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<OrdersResponse> getOrders(
        @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
        @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit,
        @RequestParam(name = "period", required = false, defaultValue = "NONE") OrderSearchPeriodCriteria period,
        @RequestParam(name = "status", required = false, defaultValue = "NONE") OrderStatusCriteria status,
        @RequestParam(name = "type", required = false, defaultValue = "NONE") OrderTypeCriteria type,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        OrderSearchCondition orderSearchCondition = OrderSearchCondition.of(page, limit, period, status, type);
        OrdersResponse response = orderService.getOrders(userId, orderSearchCondition);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/in-progress")
    public ResponseEntity<List<InprogressOrderResponse>> getInprogressOrders(
        @Auth(permit = STUDENT) Integer userId
    ) {
        List<InprogressOrderResponse> responses = orderService.getInprogressOrders(userId);
        return ResponseEntity.ok(responses);
    }
}
