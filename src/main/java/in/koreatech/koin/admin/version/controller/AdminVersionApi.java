package in.koreatech.koin.admin.version.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.version.dto.AdminVersionHistoryResponse;
import in.koreatech.koin.admin.version.dto.AdminVersionUpdateRequest;
import in.koreatech.koin.admin.version.dto.AdminVersionResponse;
import in.koreatech.koin.admin.version.dto.AdminVersionsResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(ADMIN) AdminVersion: 버전", description = "관리자 권한으로 버전 정보를 관리한다")
@RequestMapping("/admin/version")
public interface AdminVersionApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "모든 타입의 현재 버전 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping
    ResponseEntity<AdminVersionsResponse> getVersions(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 타입의 현재 버전 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/{type}")
    ResponseEntity<AdminVersionResponse> getVersion(
        @Parameter(description = """
            android, ios, android_owner, timetable, express_bus_timetable,
            shuttle_bus_timetable, city_bus_timetable
            """)
        @PathVariable("type") String type,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 타입의 현재 버전 업데이트")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/{type}")
    ResponseEntity<Void> updateVersion(
        @Parameter(description = "android, ios, android_owner") @PathVariable("type") String type,
        @RequestBody @Valid AdminVersionUpdateRequest adminVersionUpdateRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 타입의 모든 버전 이력 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/history/{type}")
    ResponseEntity<AdminVersionHistoryResponse> getHistory(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @Parameter(description = """
            android, ios, android_owner, timetable, express_bus_timetable,
            shuttle_bus_timetable, city_bus_timetable
            """)
        @PathVariable("type") String type,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
