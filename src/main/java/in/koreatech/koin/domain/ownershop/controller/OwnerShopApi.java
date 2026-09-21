package in.koreatech.koin.domain.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_USER_TYPE;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_SHOP;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;
import static in.koreatech.koin.global.code.ApiResponseCode.UNAUTHORIZED_USER;

import in.koreatech.koin.domain.ownershop.dto.OwnerShopOpenStatusRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse;
import in.koreatech.koin.domain.shop.dto.shop.request.ModifyShopRequest;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopResponse;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "(Normal) Owner Shop: 상점 (점주 전용)", description = "사장님이 상점 정보를 관리한다.")
public interface OwnerShopApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "자신의 모든 상점 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/owner/shops")
    ResponseEntity<OwnerShopsResponse> getOwnerShops(
        @Auth(permit = {OWNER}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점 생성")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owner/shops")
    ResponseEntity<Void> createOwnerShops(
        @Auth(permit = {OWNER}) Integer userId,
        @RequestBody @Valid OwnerShopsRequest ownerShopsRequest
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/owner/shops/{id}")
    ResponseEntity<ShopResponse> getOwnerShopByShopId(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable Integer id
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점 수정")
    @PutMapping("/owner/shops/{id}")
    ResponseEntity<Void> modifyOwnerShop(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("id") Integer shopId,
        @RequestBody @Valid ModifyShopRequest modifyShopRequest
    );

    @ApiResponseCodes({
        OK,
        NOT_FOUND_SHOP,
        NOT_FOUND_ORDERABLE_SHOP,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(
        summary = "상점의 영업 상태를 변경한다.",
        description = """
            ## 영업 상태 변경
            POS 화면의 영업 시작, 영업 종료에 대응한다.
            영업 시간표와는 별개로 사장님이 직접 여닫는 값이며, 영업 중이 아니면 주문을 받지 않는다.
            주문 가능 상점으로 설정되지 않은 상점은 변경할 수 없다.
            """
    )
    @SecurityRequirement(name = "Jwt Authentication")
    @PatchMapping("/owner/shops/{shopId}/open")
    ResponseEntity<Void> changeShopOpenStatus(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable Integer shopId,
        @RequestBody @Valid OwnerShopOpenStatusRequest request
    );
}
