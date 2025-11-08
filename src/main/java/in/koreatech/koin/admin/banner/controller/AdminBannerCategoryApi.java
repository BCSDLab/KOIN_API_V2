package in.koreatech.koin.admin.banner.controller;

import static in.koreatech.koin.admin.history.enums.DomainType.BANNER_CATEGORIES;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.admin.banner.dto.request.AdminBannerCategoryDescriptionModifyRequest;
import in.koreatech.koin.admin.banner.dto.response.AdminBannerCategoriesResponse;
import in.koreatech.koin.admin.banner.dto.response.AdminBannerCategoryResponse;
import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) Banner Category: 배너 카테고리", description = "어드민 배너 카테고리 정보를 관리한다")
@RequestMapping("/admin/banner-categories")
public interface AdminBannerCategoryApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "배너 카테고리 정보를 모두 조회한다.")
    @GetMapping
    ResponseEntity<AdminBannerCategoriesResponse> getCategories(
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
    @Operation(summary = "특정 배너 카테고리 설명을 수정한다")
    @PatchMapping("/{id}")
    @AdminActivityLogging(domain = BANNER_CATEGORIES, domainIdParam = "bannerCategoryId")
    ResponseEntity<AdminBannerCategoryResponse> modifyBannerCategoryDescription(
        @RequestBody @Valid AdminBannerCategoryDescriptionModifyRequest request,
        @PathVariable(name = "id") Integer bannerCategoryId,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
