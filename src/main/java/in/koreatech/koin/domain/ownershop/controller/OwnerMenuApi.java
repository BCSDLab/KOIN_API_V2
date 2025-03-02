package in.koreatech.koin.domain.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import in.koreatech.koin.domain.shop.dto.menu.request.CreateCategoryRequest;
import in.koreatech.koin.domain.shop.dto.menu.request.CreateMenuRequest;
import in.koreatech.koin.domain.shop.dto.menu.request.ModifyCategoryRequest;
import in.koreatech.koin.domain.shop.dto.menu.request.ModifyMenuRequest;
import in.koreatech.koin.domain.shop.dto.menu.response.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.menu.response.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.menu.response.ShopMenuResponse;
import in.koreatech.koin._common.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "(Normal) Owner Shop Menu: 상점 메뉴 (점주 전용)", description = "사장님이 상점 메뉴 정보를 관리한다.")
public interface OwnerMenuApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점의 메뉴 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/owner/shops/menus/{menuId}")
    ResponseEntity<MenuDetailResponse> getMenuByMenuId(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("menuId") Integer id
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점의 모든 메뉴 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/owner/shops/menus")
    ResponseEntity<ShopMenuResponse> getMenus(
        @Auth(permit = {OWNER}) Integer ownerId,
        @RequestParam("shopId") Integer shopId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점의 모든 메뉴 카테고리 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/owner/shops/menus/categories")
    ResponseEntity<MenuCategoriesResponse> getCategories(
        @Auth(permit = {OWNER}) Integer ownerId,
        @RequestParam("shopId") Integer shopId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점의 메뉴 삭제")
    @SecurityRequirement(name = "Jwt Authentication")
    @DeleteMapping("/owner/shops/menus/{menuId}")
    ResponseEntity<Void> deleteMenuByMenuId(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("menuId") Integer menuId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점의 메뉴 카테고리 삭제")
    @SecurityRequirement(name = "Jwt Authentication")
    @DeleteMapping("/owner/shops/menus/categories/{categoryId}")
    ResponseEntity<Void> deleteCategory(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("categoryId") Integer categoryId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점의 메뉴 생성")
    @PostMapping("/owner/shops/{id}/menus")
    ResponseEntity<Void> createMenu(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("id") Integer shopId,
        @RequestBody @Valid CreateMenuRequest createMenuRequest
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점의 메뉴 카테고리 생성")
    @PostMapping("/owner/shops/{id}/menus/categories")
    ResponseEntity<Void> createMenuCategory(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("id") Integer shopId,
        @RequestBody @Valid CreateCategoryRequest createCategoryRequest
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점의 메뉴 수정")
    @PutMapping("/owner/shops/menus/{menuId}")
    ResponseEntity<Void> modifyMenu(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("menuId") Integer menuId,
        @RequestBody @Valid ModifyMenuRequest modifyMenuRequest
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점의 메뉴 카테고리 수정")
    @PutMapping("/owner/shops/menus/categories/{categoryId}")
    ResponseEntity<Void> modifyMenuCategory(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("categoryId") Integer categoryId,
        @RequestBody @Valid ModifyCategoryRequest modifyCategoryRequest
    );
}
