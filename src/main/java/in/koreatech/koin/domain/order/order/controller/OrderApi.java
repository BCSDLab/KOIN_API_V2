package in.koreatech.koin.domain.order.order.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.order.order.dto.request.OrderSearchPeriodCriteria;
import in.koreatech.koin.domain.order.order.dto.request.OrderStatusCriteria;
import in.koreatech.koin.domain.order.order.dto.request.OrderTypeCriteria;
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
                - COMPLETED : 주문 완료, 포장 완료
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
        @Auth(permit = {STUDENT}) Integer userId
    );
}
