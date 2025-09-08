package in.koreatech.koin.domain.order.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.order.dto.OrderPreparingResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/order")
@Tag(name = "(Normal) Order: 주문", description = "주문을 관리한다.")
public interface OrderPreparingApi {
    @Operation(
        summary = "주문 내역 중 현재 활성화된(배달완료, 취소 제외) 주문을 조회한다",
        description = """
            ## 다음과 같은 항목을 반환한다.
            가게 이름 ex) "코인 병천점"
            주문 타입 ex) DELIVERY or TAKEOUT
            주문 상태 ex) COOKING, CONFIRMING ...
            가게 사진(썸네일) ex) abcd.jpg
            주문 내용 ex) 족발 메뉴 외 1건
            예상 시각 ex) 2025-09-07T17:45:00
            총 결제 금액 ex) 50000
            """
    )
    @GetMapping
    ResponseEntity<List<OrderPreparingResponse>> getListOrderPreparing(
        @Auth(permit = STUDENT) Integer userId
    );
}
