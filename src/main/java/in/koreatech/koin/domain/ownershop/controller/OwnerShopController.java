package in.koreatech.koin.domain.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.api.Http;

import in.koreatech.koin.domain.ownershop.dto.ModifyEventRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse;
import in.koreatech.koin.domain.ownershop.dto.ShopEventRequest;
import in.koreatech.koin.domain.ownershop.service.OwnerShopService;
import in.koreatech.koin.domain.shop.dto.CreateCategoryRequest;
import in.koreatech.koin.domain.shop.dto.CreateMenuRequest;
import in.koreatech.koin.domain.shop.dto.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.ModifyCategoryRequest;
import in.koreatech.koin.domain.shop.dto.ModifyMenuRequest;
import in.koreatech.koin.domain.shop.dto.ModifyShopRequest;
import in.koreatech.koin.domain.shop.dto.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.ShopResponse;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
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
        @RequestBody @Valid OwnerShopsRequest ownerShopsRequest
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

    @PostMapping("/owner/shops/{id}/menus")
    public ResponseEntity<Void> createMenu(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("id") Long shopId,
        @RequestBody @Valid CreateMenuRequest createMenuRequest
    ) {
        ownerShopService.createMenu(shopId, ownerId, createMenuRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/owner/shops/{id}/menus/categories")
    public ResponseEntity<Void> createMenuCategory(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("id") Long shopId,
        @RequestBody @Valid CreateCategoryRequest createCategoryRequest
    ) {
        ownerShopService.createMenuCategory(shopId, ownerId, createCategoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/owner/shops/menus/{menuId}")
    public ResponseEntity<Void> modifyMenu(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("menuId") Long menuId,
        @RequestBody @Valid ModifyMenuRequest modifyMenuRequest
    ) {
        ownerShopService.modifyMenu(ownerId, menuId, modifyMenuRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/owner/shops/menus/categories/{categoryId}")
    public ResponseEntity<Void> modifyMenuCategory(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("categoryId") Long categoryId,
        @RequestBody @Valid ModifyCategoryRequest modifyCategoryRequest
    ) {
        ownerShopService.modifyCategory(ownerId, categoryId, modifyCategoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/owner/shops/{id}")
    public ResponseEntity<Void> modifyOwnerShop(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("id") Long shopId,
        @RequestBody @Valid ModifyShopRequest modifyShopRequest
    ) {
        ownerShopService.modifyShop(ownerId, shopId, modifyShopRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/owner/shops/{id}/event")
    public ResponseEntity<Void> createShopEvent(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("id") Long shopId,
        @RequestBody @Valid ShopEventRequest shopEventRequest
    ) {
        ownerShopService.createEvent(ownerId, shopId, shopEventRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/owner/shops/{shopId}/event/{eventId}")
    public ResponseEntity<Void> modifyShopEvent(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("shopId") Long shopId,
        @PathVariable("eventId") Long eventId,
        @RequestBody @Valid ModifyEventRequest modifyEventRequest
    ) {
        ownerShopService.modifyEvent(ownerId, shopId, eventId, modifyEventRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/owner/shops/{shopId}/event/{eventId}")
    public ResponseEntity<Void> deleteShopEvent(
        @Auth(permit = {OWNER}) Long ownerId,
        @PathVariable("shopId") Long shopId,
        @PathVariable("eventId") Long eventId
    ) {
        ownerShopService.deleteEvent(ownerId, shopId, eventId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
