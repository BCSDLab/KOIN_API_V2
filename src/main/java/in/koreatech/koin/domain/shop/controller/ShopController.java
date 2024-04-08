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

    @GetMapping("/shops/{id}/menus")
    public ResponseEntity<ShopMenuResponse> findMenus(
        @PathVariable Long id
    ) {
        ShopMenuResponse shopMenuResponse = shopService.getShopMenus(id);
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
        var response = shopService.getShopEvents(shopId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shops/events")
    public ResponseEntity<ShopEventsResponse> getShopAllEvent() {
        var response = shopService.getAllEvents();
        return ResponseEntity.ok(response);
    }
}
