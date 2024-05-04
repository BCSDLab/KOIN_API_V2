package in.koreatech.koin.admin.land.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.land.dto.AdminLandsRequest;
import in.koreatech.koin.admin.land.dto.AdminLandsResponse;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) AdminLand: 복덕방", description = "관리자 권한으로 복덕방 정보를 관리한다")
public interface AdminLandApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "복덕방 목록 조회")
    @GetMapping("/admin/lands")
    ResponseEntity<AdminLandsResponse> getLands(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(name = "is_deleted", defaultValue = "false") Boolean isDeleted
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "복덕방 생성")
    @PostMapping("/admin/lands")
    ResponseEntity<AdminLandsResponse> postLands(
        @RequestBody @Valid AdminLandsRequest adminLandsRequest
    );

}
