package in.koreatech.koin.admin.banner.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.admin.banner.dto.response.AdminBannerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Admin) Banner: 배너", description = "어드민 배너 정보를 관리한다")
@RequestMapping("/admin/banner")
public interface AdminBannerApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 배너 정보를 조회한다.")
    @GetMapping("/{id}")
    ResponseEntity<AdminBannerResponse> getBanner(
        @PathVariable(name = "id") Integer bannerId,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
