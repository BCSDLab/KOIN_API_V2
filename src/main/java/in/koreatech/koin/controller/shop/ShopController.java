package in.koreatech.koin.controller.shop;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.dto.shop.ShopMenuResponse;
import in.koreatech.koin.service.shop.ShopService;

@RestController
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/shops/{shopId}/menus/{menuId}")
    public ResponseEntity<ShopMenuResponse> findMenu(@PathVariable Long shopId, @PathVariable Long menuId) {
        ShopMenuResponse shopMenu = shopService.findMenu(menuId);
        return ResponseEntity.ok(shopMenu);
    }
}
