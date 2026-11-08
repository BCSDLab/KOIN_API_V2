package in.koreatech.koin.admin.shop.controller;

import static in.koreatech.koin.admin.history.enums.DomainType.SHOP_ORDER_REQUEST;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.admin.shop.dto.shop.AdminRejectOrderServiceRequest;
import in.koreatech.koin.admin.shop.dto.shop.AdminShopOrderServiceRequestResponse;
import in.koreatech.koin.admin.shop.dto.shop.AdminShopOrderServiceRequestsResponse;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) ShopOrderServiceRequest: 주문 서비스 신청", description = "주문 서비스 신청 관리")
public interface AdminShopOrderServiceRequestApi {

    @Operation(summary = "주문 서비스 신청 목록 조회", description = "최신 신청순으로 조회합니다. 상태 미지정 시 전체 상태를 조회합니다.")
    @ApiResponseCodes({OK, UNAUTHORIZED_USER, FORBIDDEN_USER_TYPE})
    @GetMapping("/admin/shops/order-service-requests")
    ResponseEntity<AdminShopOrderServiceRequestsResponse> getRequests(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer limit,
        @RequestParam(required = false) ShopOrderServiceRequestStatus status,
        @RequestParam(required = false) String keyword,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @Operation(summary = "주문 서비스 신청 상세 조회", description = "현재 메뉴가 아닌 신청 시 저장된 이관 데이터를 반환합니다.")
    @ApiResponseCodes({OK, UNAUTHORIZED_USER, FORBIDDEN_USER_TYPE, NOT_FOUND_SHOP_ORDER_SERVICE_REQUEST})
    @GetMapping("/admin/shops/order-service-requests/{id}")
    ResponseEntity<AdminShopOrderServiceRequestResponse> getRequest(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @Operation(summary = "주문 서비스 신청 수락", description = "신청 시 저장된 데이터로 승격하고 승인 상태로 변경합니다. 영업은 닫힘으로 시작합니다.")
    @ApiResponseCodes({OK, UNAUTHORIZED_USER, FORBIDDEN_USER_TYPE, NOT_FOUND_SHOP_ORDER_SERVICE_REQUEST,
        NOT_FOUND_SHOP, ORDER_SERVICE_REQUEST_ALREADY_PROCESSED, ORDER_SERVICE_TRANSFER_DATA_REQUIRED,
        DUPLICATE_ORDERABLE_SHOP})
    @PostMapping("/admin/shops/order-service-requests/{id}/approve")
    @AdminActivityLogging(domain = SHOP_ORDER_REQUEST, domainIdParam = "id")
    ResponseEntity<Void> approve(@PathVariable Integer id, @Auth(permit = {ADMIN}) Integer adminId);

    @Operation(summary = "주문 서비스 신청 반려", description = "반려 사유를 저장합니다. 사장님 알림은 발송하지 않습니다.")
    @ApiResponseCodes({OK, UNAUTHORIZED_USER, FORBIDDEN_USER_TYPE, ILLEGAL_ARGUMENT,
        NOT_FOUND_SHOP_ORDER_SERVICE_REQUEST, ORDER_SERVICE_REQUEST_ALREADY_PROCESSED})
    @PostMapping("/admin/shops/order-service-requests/{id}/reject")
    @AdminActivityLogging(domain = SHOP_ORDER_REQUEST, domainIdParam = "id")
    ResponseEntity<Void> reject(
        @PathVariable Integer id,
        @RequestBody @Valid AdminRejectOrderServiceRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
