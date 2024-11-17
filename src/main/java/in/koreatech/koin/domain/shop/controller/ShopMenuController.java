package in.koreatech.koin.domain.shop.controller;

import in.koreatech.koin.domain.shop.dto.menu.response.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.menu.response.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.menu.response.ShopMenuResponse;
import in.koreatech.koin.domain.shop.service.ShopMenuService;
import in.koreatech.koin.domain.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShopMenuController implements ShopMenuApi {

    private final ShopMenuService shopMenuService;

    @GetMapping("/shops/{shopId}/menus/{menuId}")
    public ResponseEntity<MenuDetailResponse> findMenu(
        @PathVariable Integer shopId,
        @PathVariable Integer menuId
    ) {
        MenuDetailResponse shopMenu = shopMenuService.findMenu(menuId);
        return ResponseEntity.ok(shopMenu);
    }

    @GetMapping("/shops/{id}/menus")
    public ResponseEntity<ShopMenuResponse> findMenus(
        @PathVariable Integer id
    ) {
        ShopMenuResponse shopMenuResponse = shopMenuService.getShopMenus(id);
        return ResponseEntity.ok(shopMenuResponse);
    }

    @GetMapping("/shops/{shopId}/menus/categories")
    public ResponseEntity<MenuCategoriesResponse> getMenuCategories(
        @PathVariable Integer shopId
    ) {
        MenuCategoriesResponse menuCategories = shopMenuService.getMenuCategories(shopId);
        return ResponseEntity.ok(menuCategories);
    }
}
