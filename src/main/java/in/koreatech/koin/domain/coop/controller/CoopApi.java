package in.koreatech.koin.domain.coop.controller;

import static in.koreatech.koin.domain.user.model.UserType.COOP;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.coop.dto.SoldOutRequest;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(OWNER) Coop Dining : 영양사 식단", description = "영양사 식단 페이지")
public interface CoopApi {

    @ApiResponses(

        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "특정 코너 품절 요청")
    @PatchMapping("/coop/dining/soldout")
    ResponseEntity<Void> changeSoldOut(
        @Auth(permit = {COOP}) Long userId,
        @RequestBody SoldOutRequest soldOutRequest
    );
}
