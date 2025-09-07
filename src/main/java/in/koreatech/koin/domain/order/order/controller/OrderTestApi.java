package in.koreatech.koin.domain.order.order.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import in.koreatech.koin.domain.order.model.OrderStatus;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Profile("dev")
@Tag(name = "(Normal) Order: 주문 테스트", description = "주문 테스트 API")
public interface OrderTestApi {

    @ApiResponseCodes(
        {
            ApiResponseCode.NOT_FOUND_ORDER,
            ApiResponseCode.NOT_FOUND_USER,
            ApiResponseCode.FORBIDDEN_ORDER,
        }
    )
    @Operation(
        summary = "주문 상태 변경 테스트",
        description = """
            - orderStatus가 DELIVERED이면서 변경할 주문이 배달인 경우에는 배달 완료로 업데이트가 됩니다.
            - orderStatus가 PACKAGED이면서 변경할 주문이 배달인 경우에는 포장 완료로 업데이트가 됩니다.
            - 나머지 orderStatus에 대해서는 아무런 처리가 되지 않습니다.
            - 결제 취소의 경우 결제 취소 API를 사용해주세요.
            """
    )
    @PutMapping("/order/{orderId}/status")
    ResponseEntity<Void> updateOrderStatus(
        @PathVariable(name = "orderId") Integer orderId,
        @Auth(permit = {STUDENT}) Integer userId,
        OrderStatus orderStatus
    );
}
