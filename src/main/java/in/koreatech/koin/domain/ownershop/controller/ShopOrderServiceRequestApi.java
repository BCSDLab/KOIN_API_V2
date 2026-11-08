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
        - delivery_tip은 주문 금액과 관계없이 적용할 공통 배달비입니다.
        - transfer_data는 신청 시점의 상점 이미지와 카테고리별 메뉴·가격·이미지 데이터입니다.
        - 기존 메뉴 조회 API의 카테고리/메뉴 ID를 사용하며, 해당 상점에 속한 데이터만 제출할 수 있습니다.
        - 메뉴 데이터는 신청 시 저장되며, 승인 전에 원본 메뉴가 변경되어도 이관 데이터는 유지됩니다.
        - 이미지 목록의 첫 번째 이미지가 대표 이미지가 됩니다.
        - 이미 가입 요청이 진행 중인 가게는 중복으로 요청할 수 없습니다.
        - 이미 주문 서비스에 가입된 가게는 중복으로 요청할 수 없습니다.
        - DeliveryOption은 CAMPUS, OFF_CAMPUS, BOTH 중 하나여야 합니다.
        - (bank이름은 아직 enum처리가 되어있지 않음에 유의해주세요.)
        """)
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owner/shops/{shopId}/order-service-requests")
    @DuplicateGuard(key = "#ownerId + ':' + #shopId + ':' + #request.toString()", timeoutSeconds = 300)
    ResponseEntity<Void> createOrderableRequest(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable Integer shopId,
        @RequestBody @Valid ShopOrderServiceRequestRequest request
    );
}
