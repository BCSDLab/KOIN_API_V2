package in.koreatech.koin.domain.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.ownershop.dto.ModifyEventRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse;
import in.koreatech.koin.domain.ownershop.dto.CreateEventRequest;
import in.koreatech.koin.domain.shop.dto.CreateCategoryRequest;
import in.koreatech.koin.domain.shop.dto.CreateMenuRequest;
import in.koreatech.koin.domain.shop.dto.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.ModifyCategoryRequest;
import in.koreatech.koin.domain.shop.dto.ModifyMenuRequest;
import in.koreatech.koin.domain.shop.dto.ModifyShopRequest;
import in.koreatech.koin.domain.shop.dto.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.ShopResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Owner Shop: 상점 (점주 전용)", description = "사장님이 상점 정보를 관리한다.")
public interface OwnerShopApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "자신의 모든 상점 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/owner/shops")
    ResponseEntity<OwnerShopsResponse> getOwnerShops(
        @Auth(permit = {OWNER}) Long userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점 생성")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/owner/shops")
    ResponseEntity<Void> createOwnerShops(
        @Auth(permit = {OWNER}) Long userId,
        @RequestBody @Valid OwnerShopsRequest ownerShopsRequest
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/owner/shops/{id}")
    ResponseEntity<ShopResponse> getOwnerShopByShopId(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable Long id
    );

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
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("menuId") Long id
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
        @Auth(permit = {OWNER}) Long ownerId,
        @RequestParam("shopId") Long shopId
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
        @Auth(permit = {OWNER}) Long ownerId,
        @RequestParam("shopId") Long shopId
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
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("menuId") Long menuId
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
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("categoryId") Long categoryId
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
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("id") Long shopId,
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
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("id") Long shopId,
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
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("menuId") Long menuId,
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
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("categoryId") Long categoryId,
        @RequestBody @Valid ModifyCategoryRequest modifyCategoryRequest
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
    @Operation(summary = "상점 수정")
    @PutMapping("/owner/shops/{id}")
    ResponseEntity<Void> modifyOwnerShop(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("id") Long shopId,
        @RequestBody @Valid ModifyShopRequest modifyShopRequest
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
    @Operation(summary = "상점 이벤트 추가")
    @PostMapping("/owner/shops/{shopId}/event")
    ResponseEntity<Void> createShopEvent(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("shopId") Long shopId,
        @RequestBody @Valid CreateEventRequest shopEventRequest
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
    @Operation(summary = "상점 이벤트 수정")
    @PutMapping("/owner/shops/{shopId}/event/{eventId}")
    ResponseEntity<Void> modifyShopEvent(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("shopId") Long shopId,
        @PathVariable("eventId") Long eventId,
        @RequestBody @Valid ModifyEventRequest modifyEventRequest
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "상점 이벤트 삭제")
    @DeleteMapping("/owner/shops/{shopId}/event/{eventId}")
    ResponseEntity<Void> deleteShopEvent(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("shopId") Long shopId,
        @PathVariable("eventId") Long eventId
    );
}
