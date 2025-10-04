package in.koreatech.koin.domain.shoptoOrderable.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.shoptoOrderable.dto.ShopToOrderableRequest;
import in.koreatech.koin.domain.shoptoOrderable.dto.ShopToOrderableResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Shop To Orderable Request: 주문 서비스 가입 요청", description = "사장님이 주문 서비스 가입을 요청하기위한 API")
public interface ShopToOrderableApi {
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "사장님 주문 서비스 가입 요청")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owner/shops/orderable-requests")
    ResponseEntity<ShopToOrderableResponse> createOrderableRequest(
        @Auth(permit = {OWNER}) Integer ownerId,
        @RequestBody @Valid ShopToOrderableRequest request
    );
}
