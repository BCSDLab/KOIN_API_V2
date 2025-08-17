package in.koreatech.koin.admin.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.admin.shop.dto.menu.AdminCreateMenuCategoryRequest;
import in.koreatech.koin.admin.shop.dto.menu.AdminCreateMenuRequest;
import in.koreatech.koin.admin.shop.dto.menu.AdminMenuCategoriesResponse;
import in.koreatech.koin.admin.shop.dto.menu.AdminMenuDetailResponse;
import in.koreatech.koin.admin.shop.dto.menu.AdminModifyMenuCategoryRequest;
import in.koreatech.koin.admin.shop.dto.menu.AdminModifyMenuRequest;
import in.koreatech.koin.admin.shop.dto.menu.AdminShopMenuResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) ShopReview: 상점 메뉴", description = "관리자 권한으로 상점 메뉴를 관리한다")
public interface AdminShopMenuApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 상점의 모든 메뉴 조회")
    @GetMapping("/admin/shops/{id}/menus")
    ResponseEntity<AdminShopMenuResponse> getAllMenus(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
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
    @Operation(summary = "특정 상점의 모든 메뉴 카테고리 조회")
    @GetMapping("/admin/shops/{id}/menus/categories")
    ResponseEntity<AdminMenuCategoriesResponse> getAllMenuCategories(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
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
    @Operation(summary = "특정 상점의 메뉴 조회")
    @GetMapping("/admin/shops/{shopId}/menus/{menuId}")
    ResponseEntity<AdminMenuDetailResponse> getMenu(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @Parameter(in = PATH) @PathVariable("menuId") Integer menuId,
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
    @Operation(summary = "특정 상점의 메뉴 생성")
    @PostMapping("/admin/shops/{id}/menus")
    ResponseEntity<Void> createMenu(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
        @RequestBody @Valid AdminCreateMenuRequest request,
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
    @Operation(summary = "특정 상점의 메뉴 카테고리 생성")
    @PostMapping("/admin/shops/{id}/menus/categories")
    ResponseEntity<Void> createMenuCategory(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
        @RequestBody @Valid AdminCreateMenuCategoryRequest request,
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
    @Operation(summary = "특정 상점의 메뉴 카테고리 수정")
    @PutMapping("/admin/shops/{shopId}/menus/categories")
    ResponseEntity<Void> modifyMenuCategory(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @RequestBody @Valid AdminModifyMenuCategoryRequest request,
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
    @Operation(summary = "특정 상점의 메뉴 수정")
    @PutMapping("/admin/shops/{shopId}/menus/{menuId}")
    ResponseEntity<Void> modifyMenu(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @Parameter(in = PATH) @PathVariable("menuId") Integer menuId,
        @RequestBody @Valid AdminModifyMenuRequest request,
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
    @Operation(summary = "특정 상점의 메뉴 카테고리 삭제")
    @DeleteMapping("/admin/shops/{shopId}/menus/categories/{categoryId}")
    ResponseEntity<Void> deleteMenuCategory(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @Parameter(in = PATH) @PathVariable("categoryId") Integer categoryId,
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
    @Operation(summary = "특정 상점의 메뉴 삭제")
    @DeleteMapping("/admin/shops/{shopId}/menus/{menuId}")
    ResponseEntity<Void> deleteMenu(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @Parameter(in = PATH) @PathVariable("menuId") Integer menuId,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
