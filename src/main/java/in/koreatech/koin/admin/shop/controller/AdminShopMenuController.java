package in.koreatech.koin.admin.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.shop.dto.menu.AdminCreateMenuCategoryRequest;
import in.koreatech.koin.admin.shop.dto.menu.AdminCreateMenuRequest;
import in.koreatech.koin.admin.shop.dto.menu.AdminMenuCategoriesResponse;
import in.koreatech.koin.admin.shop.dto.menu.AdminMenuDetailResponse;
import in.koreatech.koin.admin.shop.dto.menu.AdminModifyMenuCategoryRequest;
import in.koreatech.koin.admin.shop.dto.menu.AdminModifyMenuRequest;
import in.koreatech.koin.admin.shop.dto.menu.AdminShopMenuResponse;
import in.koreatech.koin.admin.shop.service.AdminShopService;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminShopMenuController implements AdminShopMenuApi {

    private final AdminShopService adminShopService;

    @GetMapping("/admin/shops/{id}/menus")
    public ResponseEntity<AdminShopMenuResponse> getAllMenus(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminShopMenuResponse response = adminShopService.getAllMenus(shopId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/shops/{id}/menus/categories")
    public ResponseEntity<AdminMenuCategoriesResponse> getAllMenuCategories(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminMenuCategoriesResponse response = adminShopService.getAllMenuCategories(shopId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/shops/{shopId}/menus/{menuId}")
    public ResponseEntity<AdminMenuDetailResponse> getMenu(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @Parameter(in = PATH) @PathVariable("menuId") Integer menuId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminMenuDetailResponse response = adminShopService.getMenu(shopId, menuId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/shops/{shopId}/menus/categories")
    public ResponseEntity<Void> modifyMenuCategory(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @RequestBody @Valid AdminModifyMenuCategoryRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.modifyMenuCategory(shopId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/admin/shops/{shopId}/menus/{menuId}")
    public ResponseEntity<Void> modifyMenu(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @Parameter(in = PATH) @PathVariable("menuId") Integer menuId,
        @RequestBody @Valid AdminModifyMenuRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.modifyMenu(shopId, menuId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/admin/shops/{id}/menus")
    public ResponseEntity<Void> createMenu(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
        @RequestBody @Valid AdminCreateMenuRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.createMenu(shopId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/admin/shops/{id}/menus/categories")
    public ResponseEntity<Void> createMenuCategory(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
        @RequestBody @Valid AdminCreateMenuCategoryRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.createMenuCategory(shopId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/admin/shops/{shopId}/menus/categories/{categoryId}")
    public ResponseEntity<Void> deleteMenuCategory(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @Parameter(in = PATH) @PathVariable("categoryId") Integer categoryId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.deleteMenuCategory(shopId, categoryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/admin/shops/{shopId}/menus/{menuId}")
    public ResponseEntity<Void> deleteMenu(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @Parameter(in = PATH) @PathVariable("menuId") Integer menuId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.deleteMenu(shopId, menuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
