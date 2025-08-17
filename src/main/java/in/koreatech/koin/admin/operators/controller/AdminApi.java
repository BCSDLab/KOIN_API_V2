package in.koreatech.koin.admin.operators.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.abtest.useragent.UserAgent;
import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import in.koreatech.koin.admin.operators.dto.request.AdminLoginRequest;
import in.koreatech.koin.admin.operators.dto.request.AdminPasswordChangeRequest;
import in.koreatech.koin.admin.operators.dto.request.AdminPermissionUpdateRequest;
import in.koreatech.koin.admin.operators.dto.request.AdminTokenRefreshRequest;
import in.koreatech.koin.admin.operators.dto.request.AdminUpdateRequest;
import in.koreatech.koin.admin.operators.dto.response.AdminLoginResponse;
import in.koreatech.koin.admin.operators.dto.response.AdminResponse;
import in.koreatech.koin.admin.operators.dto.response.AdminTokenRefreshResponse;
import in.koreatech.koin.admin.operators.dto.response.AdminsResponse;
import in.koreatech.koin.admin.operators.dto.response.CreateAdminRequest;
import in.koreatech.koin.admin.operators.enums.TeamType;
import in.koreatech.koin.admin.operators.enums.TrackType;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) Operator: 관리자", description = "어드민 관리자 정볼를 관리한다")
public interface AdminApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "어드민 회원가입")
    @PostMapping("/admin")
    ResponseEntity<Void> createAdmin(
        @RequestBody @Valid CreateAdminRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "어드민 로그인")
    @PostMapping("/admin/user/login")
    ResponseEntity<AdminLoginResponse> adminLogin(
        @RequestBody @Valid AdminLoginRequest request,
        @UserAgent UserAgentInfo userAgentInfo
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "어드민 비밀번호 변경")
    @PutMapping("/admin/password")
    ResponseEntity<Void> adminPasswordChange(
        @RequestBody @Valid AdminPasswordChangeRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "어드민 로그아웃")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/admin/user/logout")
    ResponseEntity<Void> logout(
        @Auth(permit = {ADMIN}) Integer adminId,
        @UserAgent UserAgentInfo userAgentInfo
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "어드민 액세스 토큰 재발급")
    @PostMapping("/admin/user/refresh")
    ResponseEntity<AdminTokenRefreshResponse> refresh(
        @RequestBody @Valid AdminTokenRefreshRequest request,
        @UserAgent UserAgentInfo userAgentInfo
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "로그인 어드민 계정 정보 조회")
    @GetMapping("/admin")
    ResponseEntity<AdminResponse> getLoginAdminInfo(
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
    @Operation(summary = "어드민 계정 정보 조회")
    @GetMapping("/admin/{id}")
    ResponseEntity<AdminResponse> getAdmin(
        @PathVariable("id") Integer id,
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
    @Operation(summary = "어드민 계정 리스트 정보 조회")
    @GetMapping("/admins")
    ResponseEntity<AdminsResponse> getAdmins(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) Boolean isAuthed,
        @RequestParam(required = false) TrackType trackName,
        @RequestParam(required = false) TeamType teamName,
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
    @Operation(summary = "어드민 계정 인증 상태 변경")
    @PutMapping("/admin/{id}/authed")
    ResponseEntity<Void> adminAuthenticate(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "어드민 계정 정보 수정")
    @PutMapping("/admin/{id}")
    ResponseEntity<Void> updateAdmin(
        @RequestBody @Valid AdminUpdateRequest request,
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "어드민 계정 권한 수정")
    @PutMapping("/admin/{id}/permission")
    ResponseEntity<Void> updateAdminPermission(
        @RequestBody @Valid AdminPermissionUpdateRequest request,
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
