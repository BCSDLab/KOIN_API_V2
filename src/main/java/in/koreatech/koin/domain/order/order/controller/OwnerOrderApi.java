package in.koreatech.koin.domain.order.order.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_SHOP_OWNER;
import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_USER_TYPE;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_ORDER;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_PAYMENT;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;
import static in.koreatech.koin.global.code.ApiResponseCode.UNAUTHORIZED_USER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.order.order.dto.request.OwnerOrderStatusCriteria;
import in.koreatech.koin.domain.order.order.dto.response.OwnerOrderCountsResponse;
import in.koreatech.koin.domain.order.order.dto.response.OwnerOrderResponse;
import in.koreatech.koin.domain.order.order.dto.response.OwnerOrdersResponse;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Owner Order: 주문 (점주 전용)", description = "사장님이 주문을 조회한다.")
public interface OwnerOrderApi {

    @ApiResponseCodes({
        OK,
        NOT_FOUND_ORDERABLE_SHOP,
        FORBIDDEN_SHOP_OWNER,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(
        summary = "상점의 주문 목록을 조회한다.",
        description = """
            ## 주문 목록 조회
            POS 화면의 탭에 대응하며, 접수 일시 내림차순으로 전체를 반환한다.
            - status
                - NEW : 신규 (주문 확인중)
                - COOKING : 조리중
                - DELIVERING : 배달중
                - COMPLETED : 완료 (배달 완료, 반려)
            """
    )
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/owner/shops/{orderableShopId}/orders")
    ResponseEntity<OwnerOrdersResponse> getOrders(
        @PathVariable Integer orderableShopId,
        @RequestParam(name = "status") OwnerOrderStatusCriteria status,
        @Auth(permit = {OWNER}) Integer ownerId
    );

    @ApiResponseCodes({
        OK,
        NOT_FOUND_ORDERABLE_SHOP,
        FORBIDDEN_SHOP_OWNER,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(
        summary = "상점의 상태별 주문 수를 조회한다.",
        description = """
            ## 상태별 주문 수 조회
            POS 화면의 탭 뱃지에 표시할 숫자를 한 번에 반환한다.
            완료 수는 배달 완료와 반려를 합산한 값이다.
            """
    )
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/owner/shops/{orderableShopId}/orders/counts")
    ResponseEntity<OwnerOrderCountsResponse> getOrderCounts(
        @PathVariable Integer orderableShopId,
        @Auth(permit = {OWNER}) Integer ownerId
    );

    @ApiResponseCodes({
        OK,
        NOT_FOUND_ORDERABLE_SHOP,
        NOT_FOUND_ORDER,
        NOT_FOUND_PAYMENT,
        FORBIDDEN_SHOP_OWNER,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(
        summary = "주문 상세를 조회한다.",
        description = """
            ## 주문 상세 조회
            주문 상품과 옵션, 받는 사람 정보, 결제 정보를 함께 반환한다.
            배달 완료 일시는 배달이 끝난 주문에만, 반려 일시와 반려 사유는 반려된 주문에만 존재한다.
            요청한 상점의 주문이 아니면 404를 반환한다.
            """
    )
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/owner/shops/{orderableShopId}/orders/{orderId}")
    ResponseEntity<OwnerOrderResponse> getOrder(
        @PathVariable Integer orderableShopId,
        @PathVariable Integer orderId,
        @Auth(permit = {OWNER}) Integer ownerId
    );
}
