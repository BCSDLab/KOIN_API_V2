package in.koreatech.koin.admin.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.member.dto.AdminMembersResponse;
import in.koreatech.koin.admin.member.enums.TrackTag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Admin) AdminLand: BCSDLab 회원", description = "관리자 권한으로 BCSDLab 회원 정보를 관리한다")
public interface AdminMemberApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "페이지별 BCSDLab 회원 리스트 조회")
    @GetMapping("/admin/members")
    ResponseEntity<AdminMembersResponse> getMembers(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "50", required = false) Integer limit,
        @RequestParam(name = "track") TrackTag track,
        @RequestParam(name = "is_deleted", defaultValue = "false") Boolean isDeleted
    );
}