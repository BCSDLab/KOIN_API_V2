package in.koreatech.koin.domain.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @DeleteMapping("/owner/shops/menus/{menuId}")
    public ResponseEntity<Void> deleteMenuByMenuId(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("menuId") Long menuId
    ) {
        ownerShopService.deleteMenuByMenuId(ownerId, menuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/owner/shops/menus/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("categoryId") Long categoryId
    ) {
        ownerShopService.deleteCategory(ownerId, categoryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
