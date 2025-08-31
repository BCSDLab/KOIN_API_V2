package in.koreatech.koin.admin.member.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.member.dto.AdminMemberRequest;
import in.koreatech.koin.admin.member.dto.AdminMemberResponse;
import in.koreatech.koin.admin.member.dto.AdminMembersResponse;
import in.koreatech.koin.admin.member.enums.TrackTag;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) AdminMember: BCSDLab 회원", description = "관리자 권한으로 BCSDLab 회원 정보를 관리한다")
public interface AdminMemberApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "페이지별 BCSDLab 회원 리스트 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/admin/members")
    ResponseEntity<AdminMembersResponse> getMembers(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "50", required = false) Integer limit,
        @RequestParam(name = "track") TrackTag track,
        @RequestParam(name = "is_deleted", defaultValue = "false") Boolean isDeleted,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "BCSDLab 회원 생성")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/admin/members")
    ResponseEntity<Void> createMember(
        @RequestBody @Valid AdminMemberRequest request,
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
    @Operation(summary = "BCSDLab 회원 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/admin/members/{id}")
    ResponseEntity<AdminMemberResponse> getMember(
        @PathVariable("id") Integer memberId,
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
    @Operation(summary = "BCSDLab 회원 삭제")
    @SecurityRequirement(name = "Jwt Authentication")
    @DeleteMapping("/admin/members/{id}")
    ResponseEntity<Void> deleteMember(
        @PathVariable("id") Integer memberId,
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
    @Operation(summary = "BCSDLab 회원 수정")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/admin/members/{id}")
    ResponseEntity<Void> updateMember(
        @PathVariable("id") Integer memberId,
        @RequestBody @Valid AdminMemberRequest request,
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
    @Operation(summary = "BCSDLab 회원 삭제 취소")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/admin/members/{id}/undelete")
    ResponseEntity<Void> undeleteMember(
        @PathVariable("id") Integer memberId,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
