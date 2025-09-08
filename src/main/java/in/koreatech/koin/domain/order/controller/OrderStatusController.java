package in.koreatech.koin.domain.order.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.order.dto.OrderStatusResponse;
import in.koreatech.koin.domain.order.service.OrderStatusQueryService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderStatusController implements OrderStatusApi {

    private final OrderStatusQueryService service;

    @GetMapping("/status")
    public ResponseEntity<List<OrderStatusResponse>> getActiveOrders(
        @Auth(permit = STUDENT) Integer userId
    ) {
        return ResponseEntity.ok(service.getListLatestActive(userId));
    }
}
