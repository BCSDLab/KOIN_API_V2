package in.koreatech.koin.domain.order.controller;

import static in.koreatech.koin.domain.user.model.UserType.GENERAL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.order.dto.OrderStatusResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "진행 중 주문 플로팅", description = "진행 중인 주문을 조회한다")
@RequestMapping("/order")
public interface OrderStatusApi {
    @Operation(
        summary = "진행 중인 주문 내역을 조회한다.",
        description = """
            예상 시각,
            가게 이름,
            배달 or 포장
            """
    )

    @GetMapping
    ResponseEntity<List<OrderStatusResponse>> listActiveOrders(
        @Auth(permit = {GENERAL, STUDENT}) Integer userId
    );
}
