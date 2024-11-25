package in.koreatech.koin.admin.owner.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.admin.owner.dto.AdminNewOwnersResponse;
import in.koreatech.koin.admin.owner.dto.AdminOwnerResponse;
import in.koreatech.koin.admin.owner.dto.AdminOwnerUpdateRequest;
import in.koreatech.koin.admin.owner.dto.AdminOwnerUpdateResponse;
import in.koreatech.koin.admin.owner.dto.AdminOwnersResponse;
import in.koreatech.koin.admin.owner.dto.OwnersCondition;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) Owner: 사장님", description = "관리자 권한으로 사장님 정보를 관리한다")
public interface AdminOwnerApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "사장님 권한 요청 허용")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/admin/owner/{id}/authed")
    ResponseEntity<Void> allowOwnerPermission(
        @PathVariable Integer id,
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
    @Operation(summary = "특정 사장님 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/admin/users/owner/{id}")
    ResponseEntity<AdminOwnerResponse> getOwner(
        @PathVariable Integer id,
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
    @Operation(summary = "특정 사장님 수정")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/admin/users/owner/{id}")
    ResponseEntity<AdminOwnerUpdateResponse> updateOwner(
        @PathVariable Integer id,
        @RequestBody @Valid AdminOwnerUpdateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "가입 신청한 사장님 리스트 조회 (페이지네이션)")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/admin/users/new-owners")
    ResponseEntity<AdminNewOwnersResponse> getNewOwners(
        @ParameterObject @ModelAttribute OwnersCondition ownersCondition,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "사장 리스트 조회 (페이지네이션)")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/admin/users/owners")
    ResponseEntity<AdminOwnersResponse> getOwners(
        @ParameterObject @ModelAttribute OwnersCondition ownersCondition,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
