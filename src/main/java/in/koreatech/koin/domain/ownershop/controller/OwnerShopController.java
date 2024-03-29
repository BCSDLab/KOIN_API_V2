package in.koreatech.koin.domain.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.ownershop.dto.OwnerShopsRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse;
import in.koreatech.koin.domain.ownershop.service.OwnerShopService;
import in.koreatech.koin.domain.shop.dto.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.ShopResponse;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OwnerShopController implements OwnerShopApi {

    private final OwnerShopService ownerShopService;

    @GetMapping("/owner/shops")
    public ResponseEntity<OwnerShopsResponse> getOwnerShops(
        @Auth(permit = {OWNER}) Long ownerId
    ) {
        OwnerShopsResponse ownerShopsResponses = ownerShopService.getOwnerShops(ownerId);
        return ResponseEntity.ok().body(ownerShopsResponses);
    }

    @PostMapping("/owner/shops")
    public ResponseEntity<Void> createOwnerShops(
        @Auth(permit = {OWNER}) Long ownerId,
        @RequestBody OwnerShopsRequest ownerShopsRequest
    ) {
        ownerShopService.createOwnerShops(ownerId, ownerShopsRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/owner/shops/{id}")
    public ResponseEntity<ShopResponse> getOwnerShopByShopId(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable Long id
    ) {
        ShopResponse shopResponse = ownerShopService.getShopByShopId(ownerId, id);
        return ResponseEntity.ok(shopResponse);
    }

    @GetMapping("/owner/shops/menus/{menuId}")
    public ResponseEntity<MenuDetailResponse> getMenuByMenuId(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable Long menuId
    ) {
        MenuDetailResponse menuDetailResponse = ownerShopService.getMenuByMenuId(ownerId, menuId);
        return ResponseEntity.ok(menuDetailResponse);
    }

    @GetMapping("/owner/shops/menus")
    public ResponseEntity<ShopMenuResponse> getMenus(
        @Auth(permit = {OWNER}) Long ownerId,
        @RequestParam("shopId") Long shopId
    ) {
        ShopMenuResponse shopMenuResponse = ownerShopService.getMenus(shopId, ownerId);
        return ResponseEntity.ok(shopMenuResponse);
    }

    @GetMapping("/owner/shops/menus/categories")
    public ResponseEntity<MenuCategoriesResponse> getCategories(
        @Auth(permit = {OWNER}) Long ownerId,
        @RequestParam("shopId") Long shopId
    ) {
        MenuCategoriesResponse menuCategoriesResponse = ownerShopService.getCategories(shopId, ownerId);
        return ResponseEntity.ok(menuCategoriesResponse);
    }
}
