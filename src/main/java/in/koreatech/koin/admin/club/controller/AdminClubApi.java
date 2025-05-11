package in.koreatech.koin.admin.club.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.admin.club.dto.request.CreateAdminClubRequest;
import in.koreatech.koin.admin.club.dto.response.AdminClubResponse;
import in.koreatech.koin.admin.club.dto.response.AdminClubsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) Club Category: 동아리", description = "어드민 동아리 정보를 관리한다")
@RequestMapping("/admin/clubs")
public interface AdminClubApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리 정보를 페이지네이션으로 조회한다.", description = """
        club_category 기본값은 "전체"이고, is_hits 기본값은 false 입니다.
        """)
    @GetMapping
    ResponseEntity<AdminClubsResponse> getClubs(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(name = "sort_by_like", required = false) Boolean sortByLike,
        @RequestParam(name = "club_category", required = false, defaultValue = "전체") String clubCategory,
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
    @Operation(summary = "특정 동아리 정보를 조회한다.")
    @GetMapping("/{id}")
    ResponseEntity<AdminClubResponse> getClub(
        @PathVariable(value = "id") Integer clubId,
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
    @Operation(summary = "동아리를 생성한다")
    @PostMapping
    ResponseEntity<Void> createClub(
        @RequestBody @Valid CreateAdminClubRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
