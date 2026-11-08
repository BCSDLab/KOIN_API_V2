package in.koreatech.koin.admin.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.shop.dto.shop.AdminShopOrderServiceRequestsResponse;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
}
