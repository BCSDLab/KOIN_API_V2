package in.koreatech.koin.domain.order.order.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.order.order.model.OrderStatus;
import in.koreatech.koin.domain.order.order.service.OrderTestService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@Profile("dev")
@RestController
@RequiredArgsConstructor
public class OrderTestController implements OrderTestApi {

    private final OrderTestService orderTestService;

    @PutMapping("/order/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
        @PathVariable(name = "id") Integer orderId,
        @Auth(permit = {STUDENT}) Integer userId,
        OrderStatus orderStatus
    ) {
        orderTestService.updateOrderStatus(orderId, userId, orderStatus);
        return ResponseEntity.ok().build();
    }
}
