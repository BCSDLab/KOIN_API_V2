package in.koreatech.koin.admin.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.*;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServiceRequestProcessRequest;
import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServiceResponse;
import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServicesResponse;
import in.koreatech.koin.admin.ownershop.dto.ShopOrderServiceRequestCondition;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) ShopOrderServiceRequest: 주문 서비스 요청", description = "관리자 권한으로 주문 서비스 요청 정보를 관리한다")
public interface AdminShopOrderServiceRequestApi {

    @ApiResponseCodes({
        OK,
        REQUIRED_SEARCH_TYPE,
        SEARCH_QUERY_ONLY_WHITESPACE
    })
    @Operation(summary = "주문 서비스 요청 리스트 조회 (페이지네이션)")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/admin/owner/shops/order-service-requests")
    ResponseEntity<AdminShopOrderServicesResponse> getOrderServiceRequests(
        @ParameterObject @ModelAttribute ShopOrderServiceRequestCondition shopOrderServiceRequestCondition,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponseCodes({
        OK,
        NOT_FOUND_SHOP_ORDER_SERVICE_REQUEST
    })
    @Operation(summary = "주문 서비스 요청 상세 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/admin/owner/shops/order-service-requests/{orderServiceRequestId}")
    ResponseEntity<AdminShopOrderServiceResponse> getOrderServiceRequest(
        @PathVariable("orderServiceRequestId") Integer orderServiceRequestId,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponseCodes({
        OK,
        UNAUTHORIZED_USER,
        FORBIDDEN_ADMIN,
        FORBIDDEN_USER_TYPE,
        INVALID_REQUEST_BODY,
        NOT_FOUND_SHOP_ORDER_SERVICE_REQUEST,
        NOT_FOUND_SHOP,
        DUPLICATE_ORDERABLE_SHOP,
        ORDER_SERVICE_REQUEST_ALREADY_PROCESSED,
        ORDER_SERVICE_REQUEST_INCOMPLETE
    })
    @Operation(summary = "주문 서비스 신청 처리", description = """
        대기 중인 주문 서비스 신청을 처리합니다.
        - `APPROVE`: 신청에 저장된 정보와 메뉴를 이관해 상점을 주문가능상점으로 승격합니다.
        - `REJECT`: 반려 사유를 저장하고 신청을 반려합니다.
        - `rejection_reason`은 반려 시에만 입력합니다.
        """)
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/admin/owner/shops/order-service-requests/{id}/process")
    ResponseEntity<Void> processOrderServiceRequest(
        @Parameter(in = PATH) @PathVariable("id") Integer id,
        @RequestBody @Valid AdminShopOrderServiceRequestProcessRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
