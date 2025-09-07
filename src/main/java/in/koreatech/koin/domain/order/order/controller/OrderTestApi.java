package in.koreatech.koin.domain.order.order.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import in.koreatech.koin.domain.order.model.OrderStatus;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Profile("dev")
@Tag(name = "(Normal) Order: 주문 테스트", description = "주문 테스트 API")
public interface OrderTestApi {

    @Operation(summary = "주문 상태 변경 테스트")
    @PutMapping("/order/{orderId}/status")
    ResponseEntity<Void> updateOrderStatus(
        @PathVariable(name = "orderId") Integer orderId,
        @Auth(permit = {STUDENT}) Integer userId,
        OrderStatus orderStatus
    );
}
