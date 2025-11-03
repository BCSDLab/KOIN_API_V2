package in.koreatech.koin.admin.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServiceResponse;
import in.koreatech.koin.admin.ownershop.dto.ShopOrderServiceRequestCondition;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Admin)  ShopOrderServieRequest: 주문 서비스 요청", description = "관리자 권한으로 주문 서비스 요청 정보를 관리한다")
public interface AdminShopOrderServiceRequestApi {

    @ApiResponses(
        value = {
            //작성
        }
    )
    @Operation(summary = " 주문 서비스 요청한 리스트 조회 (페이지네이션)")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/admin/owner/shops/order-service-requests")
    ResponseEntity<AdminShopOrderServiceResponse> getOrderServiceRequests(
        @ParameterObject @ModelAttribute ShopOrderServiceRequestCondition shopOrderServiceRequestCondition,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
