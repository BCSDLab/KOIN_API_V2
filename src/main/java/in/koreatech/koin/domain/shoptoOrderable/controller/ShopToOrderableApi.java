package in.koreatech.koin.domain.shoptoOrderable.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.shoptoOrderable.dto.ShopToOrderableRequest;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Shop To Orderable Request: 주문 서비스 가입 요청", description = "사장님이 주문 서비스 가입을 요청하기위한 API")
public interface ShopToOrderableApi {

    @ApiResponseCodes({
        ApiResponseCode.OK,
        ApiResponseCode.ILLEGAL_ARGUMENT,
        ApiResponseCode.FORBIDDEN_USER_TYPE,
        ApiResponseCode.NOT_FOUND_USER,
        ApiResponseCode.NOT_FOUND_SHOP,
    })
    @Operation(summary = "사장님 주문 서비스 가입 요청")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owner/shops/{shopId}/orderable-requests")
    ResponseEntity<Void> createOrderableRequest(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable Integer shopId,
        @RequestBody @Valid ShopToOrderableRequest request
    );
}
