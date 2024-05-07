package in.koreatech.koin.domain.coop.controller;

import static in.koreatech.koin.domain.user.model.UserType.COOP;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.coop.dto.DiningImageRequest;
import in.koreatech.koin.domain.coop.dto.SoldOutRequest;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RequestMapping("/coop")
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
    @PatchMapping("/dining/soldout")
    ResponseEntity<Void> changeSoldOut(
        @Auth(permit = {COOP}) Integer userId,
        @RequestBody @Valid SoldOutRequest soldOutRequest
    );

    @ApiResponses(

        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "이미지 사진 업로드")
    @PatchMapping("/dining/image")
    ResponseEntity<Void> saveDiningImage(
        @Auth(permit = {COOP}) Integer userId,
        @RequestBody @Valid DiningImageRequest imageRequest
    );
}
