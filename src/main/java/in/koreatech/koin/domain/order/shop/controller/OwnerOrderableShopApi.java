package in.koreatech.koin.domain.order.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.order.shop.dto.OwnerOrderableShopOpenStatusRequest;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Owner OrderableShop: 주문 가능 상점 (점주 전용)", description = "사장님이 주문 가능 상점을 관리한다.")
public interface OwnerOrderableShopApi {

    @ApiResponseCodes({
        OK,
        NOT_FOUND_ORDERABLE_SHOP,
        FORBIDDEN_SHOP_OWNER,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(
        summary = "주문 가능 상점의 영업 상태를 변경한다.",
        description = """
            ## 영업 상태 변경
            POS 화면의 영업 시작, 영업 종료에 대응한다.
            영업 시간표와는 별개로 사장님이 직접 여닫는 값이며, 영업 중이 아니면 주문을 받지 않는다.
            같은 값으로 다시 요청해도 결과는 동일하다.
            """
    )
    @SecurityRequirement(name = "Jwt Authentication")
    @PatchMapping("/owner/shops/{orderableShopId}/open")
    ResponseEntity<Void> changeOpenStatus(
        @PathVariable Integer orderableShopId,
        @RequestBody @Valid OwnerOrderableShopOpenStatusRequest request,
        @Auth(permit = {OWNER}) Integer ownerId
    );
}
