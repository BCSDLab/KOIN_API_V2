package in.koreatech.koin.admin.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.shop.service.AdminShopService;
import in.koreatech.koin.domain.shop.dto.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.ShopCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.ShopEventsResponse;
import in.koreatech.koin.domain.shop.dto.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.ShopResponse;
import in.koreatech.koin.domain.shop.dto.ShopsResponse;
import in.koreatech.koin.domain.shop.service.ShopService;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminShopController implements AdminShopApi {

    private final AdminShopService adminShopService;

    @GetMapping("/admin/shops/{id}/menus")
    public ResponseEntity<MenuDetailResponse> getAllMenus(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {

    }

    @GetMapping("/admin/shops/{id}/menus/categories")
    public ResponseEntity<MenuDetailResponse> getAllMenuCategories(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {

    }

    @GetMapping("/admin/shops/{shopId}/menus/{menuId}")
    public ResponseEntity<MenuDetailResponse> getMenu(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @Parameter(in = PATH) @PathVariable("menuId") Integer menuId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {

    }
}
