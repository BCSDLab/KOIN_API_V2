package in.koreatech.koin.domain.shop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.shop.dto.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.ShopCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.ShopEventsResponse;
import in.koreatech.koin.domain.shop.dto.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.ShopResponse;
import in.koreatech.koin.domain.shop.dto.ShopsResponse;
import in.koreatech.koin.domain.shop.service.ShopService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ShopController implements ShopApi {

    private final ShopService shopService;

    @GetMapping("/shops/{shopId}/menus/{menuId}")
    public ResponseEntity<MenuDetailResponse> findMenu(
        @PathVariable Long shopId,
        @PathVariable Long menuId
    ) {
        MenuDetailResponse shopMenu = shopService.findMenu(menuId);
        return ResponseEntity.ok(shopMenu);
    }

    public ResponseEntity<ShopMenuResponse> findMenu(Long id) {
        ShopMenuResponse shopMenuResponse = shopService.getShopMenu(id);
        return ResponseEntity.ok(shopMenuResponse);
    }

    @GetMapping("/shops/{shopId}/menus/categories")
    public ResponseEntity<MenuCategoriesResponse> getMenuCategories(
        @PathVariable Long shopId
    ) {
        MenuCategoriesResponse menuCategories = shopService.getMenuCategories(shopId);
        return ResponseEntity.ok(menuCategories);
    }

    @GetMapping("/shops/{id}")
    public ResponseEntity<ShopResponse> getShopById(
        @PathVariable Long id
    ) {
        ShopResponse shopResponse = shopService.getShop(id);
        return ResponseEntity.ok(shopResponse);
    }

    @GetMapping("/shops")
    public ResponseEntity<ShopsResponse> getShops() {
        ShopsResponse shopsResponse = shopService.getShops();
        return ResponseEntity.ok(shopsResponse);
    }

    @GetMapping("/shops/categories")
    public ResponseEntity<ShopCategoriesResponse> getShopsCategories() {
        ShopCategoriesResponse shopCategoriesResponse = shopService.getShopsCategories();
        return ResponseEntity.ok(shopCategoriesResponse);
    }

    @GetMapping("/shops/{shopId}/events")
    public ResponseEntity<ShopEventsResponse> getShopEvents(
        @PathVariable Long shopId
    ) {

        var response = shopService.getEvents(shopId);
        return ResponseEntity.ok(response);
    }
}
