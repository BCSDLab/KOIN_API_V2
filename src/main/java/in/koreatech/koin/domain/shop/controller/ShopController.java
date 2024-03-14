package in.koreatech.koin.domain.shop.controller;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.shop.dto.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.ShopResponse;
import in.koreatech.koin.domain.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ShopController implements ShopApi {

    private final ShopService shopService;

    @Override
    @GetMapping("/shops/{shopId}/menus/{menuId}")
    public ResponseEntity<ShopMenuResponse> findMenu(
        @PathVariable Long shopId,
        @PathVariable Long menuId
    ) {
        ShopMenuResponse shopMenu = shopService.findMenu(menuId);
        return ResponseEntity.ok(shopMenu);
    }

    @Override
    @GetMapping("/shops/{shopId}/menus/categories")
    public ResponseEntity<MenuCategoriesResponse> getMenuCategories(
        @PathVariable Long shopId
    ) {
        MenuCategoriesResponse menuCategories = shopService.getMenuCategories(shopId);
        return ResponseEntity.ok(menuCategories);
    }

    @Override
    @GetMapping("/shops/{id}")
    public ResponseEntity<ShopResponse> getShopById(@PathVariable Long id) {
        ShopResponse shopResponse = shopService.getShop(id);
        return ResponseEntity.ok(shopResponse);
    }
}
