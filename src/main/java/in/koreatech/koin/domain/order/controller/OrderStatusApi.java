package in.koreatech.koin.domain.order.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.order.dto.OrderStatusResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Order: 주문", description = "주문을 관리한다.")
@RequestMapping("/order")
public interface OrderStatusApi {
    @Operation(
        summary = "주문 내역 중 현재 활성화된(배달완료, 취소 제외) 최근 한 건의 주문을 조회한다.",
        description = """
            ## 다음과 같은 항목을 반환한다.
            가게 이름 ex) "코인 병천점"
            주문 타입 ex) DELIVERY or TAKEOUT
            주문 상태 ex) COOKING, CONFIRMING ...
            예상 시각 ex) 17:20
            """
    )
    @GetMapping
    ResponseEntity<List<OrderStatusResponse>> getActiveOrders(
        @Auth(permit = STUDENT) Integer userId
    );
}
