package in.koreatech.koin.domain.order.order.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.order.order.dto.request.OrderSearchPeriodCriteria;
import in.koreatech.koin.domain.order.order.dto.request.OrderStatusCriteria;
import in.koreatech.koin.domain.order.order.dto.request.OrderTypeCriteria;
import in.koreatech.koin.domain.order.order.dto.response.InprogressOrderResponse;
import in.koreatech.koin.domain.order.order.dto.response.OrdersResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Order: 주문", description = "주문을 관리한다.")
@RequestMapping("/order")
public interface OrderApi {

    @Operation(
        summary = "주문 이력을 조회한다.",
        description = """
            ## 주문 이력 조회
            - period
                - NONE : 기본값
                - LAST_3_MONTHS : 최근 3개월
                - LAST_6_MONTHS : 최근 3개월
                - LAST_1_YEAR : 최근 1년
            - status
                - NONE : 기본값
                - COMPLETED : 주문 완료, 포장 수령
                - CANCELED : 주문 취소
            - type
                - NONE : 기본값
                - DELIVERY : 배달
                - TAKE_OUT : 포장
            """
    )
    @GetMapping
    ResponseEntity<OrdersResponse> getOrders(
        @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
        @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit,
        @RequestParam(name = "period", required = false, defaultValue = "NONE") OrderSearchPeriodCriteria period,
        @RequestParam(name = "status", required = false, defaultValue = "NONE") OrderStatusCriteria status,
        @RequestParam(name = "type", required = false, defaultValue = "NONE") OrderTypeCriteria type,
        @RequestParam(name = "query", required = false, defaultValue = "") String query,
        @Auth(permit = {STUDENT}) Integer userId
    );

    @Operation(
        summary = "주문 내역 중 현재 활성화된(배달완료, 취소 제외) 주문을 조회한다",
        description = """
            ## 다음과 같은 항목을 반환한다.
            - 주문 가능 상점 ID ex) 14
            - 가게 이름 ex) "코인 병천점"
            - 주문 타입 ex)
                DELIVERY
                TAKE_OUT
            - 주문 상태 ex)
                CONFIRMING
                COOKING
                PACKAGED
                PICKED_UP
                DELIVERING
                DELIVERED
                CANCELED
            - 가게 사진(썸네일) ex) https://static.koreatech.in/test.png
            - 주문 내용 ex) 족발 메뉴 외 1건
            - 예상 시각 ex) 17:45
            - 총 결제 금액 ex) 50000
            """
    )
    @GetMapping("/in-progress")
    ResponseEntity<List<InprogressOrderResponse>> getInprogressOrders(
        @Auth(permit = STUDENT) Integer userId
    );
}
