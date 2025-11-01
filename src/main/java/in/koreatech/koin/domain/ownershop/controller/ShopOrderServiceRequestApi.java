package in.koreatech.koin.domain.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.ownershop.dto.ShopOrderServiceRequestRequest;
import in.koreatech.koin.global.auth.Auth;

import in.koreatech.koin.global.code.ApiResponseCodes;
import in.koreatech.koin.global.duplicate.DuplicateGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import static in.koreatech.koin.global.code.ApiResponseCode.*;

@Tag(name = "(Normal) ShopOrderServieRequest: 주문 서비스 요청 (점주 전용)", description = "주문 서비스 요청 관련 API")
public interface ShopOrderServiceRequestApi {

    @ApiResponseCodes({
        OK,
        ILLEGAL_ARGUMENT,
        FORBIDDEN_USER_TYPE,
        NOT_FOUND_USER,
        NOT_FOUND_SHOP,
        FORBIDDEN_SHOP_OWNER,
        DUPLICATE_REQUESTED_ORDERABLE_SHOP,
        DUPLICATE_ORDERABLE_SHOP
    })
    @Operation(summary = "사장님 주문 서비스 가입 요청", description = """
        ### 사장님이 자신이 소유한 가게를 주문 서비스 가입요청을 합니다.
        - 요청자는 점주(OWNER)만 가능합니다.
        - NULL 값이 들어올 수 있는 필드는 없습니다.
        - 이미 가입 요청이 진행 중인 가게는 중복으로 요청할 수 없습니다.
        - 이미 주문 서비스에 가입된 가게는 중복으로 요청할 수 없습니다.
        - DeliveryOption은 CAMPUS, OFF_CAMPUS, BOTH 중 하나여야 합니다.
        - (bank이름은 아직 enum처리가 되어있지 않음에 유의해주세요.)
        """)
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owner/shops/{shopId}/orderable-requests")
    @DuplicateGuard(key = "#ownerId + ':' + #shopId + ':' + #request.toString()", timeoutSeconds = 300)
    ResponseEntity<Void> createOrderableRequest(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable Integer shopId,
        @RequestBody @Valid ShopOrderServiceRequestRequest request
    );
}
