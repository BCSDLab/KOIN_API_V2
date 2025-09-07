package in.koreatech.koin.domain.order.order.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.order.order.dto.response.OrdersResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Order: 주문", description = "주문을 관리한다.")
@RequestMapping("/order")
public interface OrderApi {

    @GetMapping
    ResponseEntity<OrdersResponse> getOrders(
        @Auth(permit = {STUDENT}) Integer userId
    );
}
