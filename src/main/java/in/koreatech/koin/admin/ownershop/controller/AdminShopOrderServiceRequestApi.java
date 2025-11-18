package in.koreatech.koin.admin.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServiceResponse;
import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServicesResponse;
import in.koreatech.koin.admin.ownershop.dto.ShopOrderServiceRequestCondition;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

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
        NOT_FOUND_SHOP_ORDER_SERVICE_REQUEST,
        NOT_PENDING_REQUEST
    })
    @Operation(summary = "주문 서비스 요청 승인")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/admin/owner/shops/order-service-requests/{orderServiceRequestId}/approve")
    ResponseEntity<Void> approveOrderServiceRequest(
        @PathVariable("orderServiceRequestId") Integer orderServiceRequestId,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponseCodes({
        OK,
        NOT_FOUND_SHOP_ORDER_SERVICE_REQUEST,
        NOT_PENDING_REQUEST
    })
    @Operation(summary = "주문 서비스 요청 거절")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/admin/owner/shops/order-service-requests/{orderServiceRequestId}/reject")
    ResponseEntity<Void> rejectOrderServiceRequest(
        @PathVariable("orderServiceRequestId") Integer orderServiceRequestId,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
