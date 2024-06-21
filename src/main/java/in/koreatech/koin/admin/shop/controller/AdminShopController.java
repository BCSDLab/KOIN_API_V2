package in.koreatech.koin.admin.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.shop.dto.AdminMenuCategoriesResponse;
import in.koreatech.koin.admin.shop.dto.AdminMenuDetailResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopMenuResponse;
import in.koreatech.koin.admin.shop.service.AdminShopService;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminShopController implements AdminShopApi {

    private final AdminShopService adminShopService;

    @GetMapping("/admin/shops/{id}/menus")
    public ResponseEntity<AdminShopMenuResponse> getAllMenus(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminShopMenuResponse adminShopMenuResponse = adminShopService.getAllMenus(shopId);
        return ResponseEntity.ok(adminShopMenuResponse);
    }

    @GetMapping("/admin/shops/{id}/menus/categories")
    public ResponseEntity<AdminMenuCategoriesResponse> getAllMenuCategories(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminMenuCategoriesResponse adminMenuCategoriesResponse = adminShopService.getAllMenuCategories(shopId);
        return ResponseEntity.ok(adminMenuCategoriesResponse);
    }

    @GetMapping("/admin/shops/{shopId}/menus/{menuId}")
    public ResponseEntity<AdminMenuDetailResponse> getMenu(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @Parameter(in = PATH) @PathVariable("menuId") Integer menuId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminMenuDetailResponse adminMenuDetailResponse = adminShopService.getMenu(shopId, menuId);
        return ResponseEntity.ok(adminMenuDetailResponse);
    }
}
