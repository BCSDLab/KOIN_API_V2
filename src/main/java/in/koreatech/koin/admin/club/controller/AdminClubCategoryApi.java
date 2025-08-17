package in.koreatech.koin.admin.club.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.admin.club.dto.response.AdminClubCategoriesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Admin) Club Category: 동아리 카테고리", description = "어드민 동아리 카테고리 정보를 관리한다")
@RequestMapping("/admin/clubs/categories")
public interface AdminClubCategoryApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리 카테고리 정보를 모두 조회한다.")
    @GetMapping
    ResponseEntity<AdminClubCategoriesResponse> getClubCategories(
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
