package in.koreatech.koin.admin.updateversion.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.updateversion.dto.AdminUpdateHistoryResponse;
import in.koreatech.koin.admin.updateversion.dto.AdminUpdateVersionRequest;
import in.koreatech.koin.admin.updateversion.dto.AdminUpdateVersionResponse;
import in.koreatech.koin.admin.updateversion.dto.AdminUpdateVersionsResponse;
import in.koreatech.koin.domain.updateversion.model.UpdateVersionType;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(ADMIN) Update Version: 업데이트 버전", description = "업데이트 버전 정보를 관리한다")
@RequestMapping("/admin/update")
public interface AdminUpdateVersionApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "모든 타입의 버전 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/version")
    ResponseEntity<AdminUpdateVersionsResponse> getVersions(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 타입의 버전 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/version/{type}")
    ResponseEntity<AdminUpdateVersionResponse> getVersion(
        @PathVariable("type") UpdateVersionType type,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 타입의 버전 업데이트")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/version/{type}")
    ResponseEntity<Void> updateVersion(
        @PathVariable("type") UpdateVersionType type,
        @RequestBody @Valid AdminUpdateVersionRequest adminUpdateVersionRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 타입의 버전 업데이트 이력 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/version/history/{type}")
    ResponseEntity<AdminUpdateHistoryResponse> getHistory(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @PathVariable("type") UpdateVersionType type,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
