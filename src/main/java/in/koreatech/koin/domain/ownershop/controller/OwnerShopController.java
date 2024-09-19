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

import in.koreatech.koin.domain.ownershop.dto.CreateEventRequest;
import in.koreatech.koin.domain.ownershop.dto.ModifyEventRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopEventsResponse;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse;
import in.koreatech.koin.domain.ownershop.service.OwnerShopService;
import in.koreatech.koin.domain.shop.dto.menu.CreateCategoryRequest;
import in.koreatech.koin.domain.shop.dto.menu.CreateMenuRequest;
import in.koreatech.koin.domain.shop.dto.menu.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.menu.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.menu.ModifyCategoryRequest;
import in.koreatech.koin.domain.shop.dto.menu.ModifyMenuRequest;
import in.koreatech.koin.domain.shop.dto.shop.ModifyShopRequest;
import in.koreatech.koin.domain.shop.dto.menu.ShopMenuResponse;
import in.koreatech.koin.domain.shop.dto.shop.ShopResponse;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OwnerShopController implements OwnerShopApi {

    private final OwnerShopService ownerShopService;

    @GetMapping("/owner/shops")
    public ResponseEntity<OwnerShopsResponse> getOwnerShops(
        @Auth(permit = {OWNER}) Integer ownerId
    ) {
        OwnerShopsResponse ownerShopsResponses = ownerShopService.getOwnerShops(ownerId);
        return ResponseEntity.ok().body(ownerShopsResponses);
    }

    @PostMapping("/owner/shops")
    public ResponseEntity<Void> createOwnerShops(
        @Auth(permit = {OWNER}) Integer ownerId,
        @RequestBody @Valid OwnerShopsRequest ownerShopsRequest
    ) {
        ownerShopService.createOwnerShops(ownerId, ownerShopsRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/owner/shops/{id}")
    public ResponseEntity<ShopResponse> getOwnerShopByShopId(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable Integer id
    ) {
        ShopResponse shopResponse = ownerShopService.getShopByShopId(ownerId, id);
        return ResponseEntity.ok(shopResponse);
    }

    @GetMapping("/owner/shops/menus/{menuId}")
    public ResponseEntity<MenuDetailResponse> getMenuByMenuId(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable Integer menuId
    ) {
        MenuDetailResponse menuDetailResponse = ownerShopService.getMenuByMenuId(ownerId, menuId);
        return ResponseEntity.ok(menuDetailResponse);
    }

    @GetMapping("/owner/shops/menus")
    public ResponseEntity<ShopMenuResponse> getMenus(
        @Auth(permit = {OWNER}) Integer ownerId,
        @RequestParam("shopId") Integer shopId
    ) {
        ShopMenuResponse shopMenuResponse = ownerShopService.getMenus(shopId, ownerId);
        return ResponseEntity.ok(shopMenuResponse);
    }

    @GetMapping("/owner/shops/menus/categories")
    public ResponseEntity<MenuCategoriesResponse> getCategories(
        @Auth(permit = {OWNER}) Integer ownerId,
        @RequestParam("shopId") Integer shopId
    ) {
        MenuCategoriesResponse menuCategoriesResponse = ownerShopService.getCategories(shopId, ownerId);
        return ResponseEntity.ok(menuCategoriesResponse);
    }

    @DeleteMapping("/owner/shops/menus/{menuId}")
    public ResponseEntity<Void> deleteMenuByMenuId(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("menuId") Integer menuId
    ) {
        ownerShopService.deleteMenuByMenuId(ownerId, menuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/owner/shops/menus/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("categoryId") Integer categoryId
    ) {
        ownerShopService.deleteCategory(ownerId, categoryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/owner/shops/{id}/menus")
    public ResponseEntity<Void> createMenu(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("id") Integer shopId,
        @RequestBody @Valid CreateMenuRequest createMenuRequest
    ) {
        ownerShopService.createMenu(shopId, ownerId, createMenuRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/owner/shops/{id}/menus/categories")
    public ResponseEntity<Void> createMenuCategory(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("id") Integer shopId,
        @RequestBody @Valid CreateCategoryRequest createCategoryRequest
    ) {
        ownerShopService.createMenuCategory(shopId, ownerId, createCategoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/owner/shops/menus/{menuId}")
    public ResponseEntity<Void> modifyMenu(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("menuId") Integer menuId,
        @RequestBody @Valid ModifyMenuRequest modifyMenuRequest
    ) {
        ownerShopService.modifyMenu(ownerId, menuId, modifyMenuRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/owner/shops/menus/categories/{categoryId}")
    public ResponseEntity<Void> modifyMenuCategory(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("categoryId") Integer categoryId,
        @RequestBody @Valid ModifyCategoryRequest modifyCategoryRequest
    ) {
        ownerShopService.modifyCategory(ownerId, categoryId, modifyCategoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/owner/shops/{id}")
    public ResponseEntity<Void> modifyOwnerShop(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("id") Integer shopId,
        @Valid @RequestBody ModifyShopRequest modifyShopRequest
    ) {
        ownerShopService.modifyShop(ownerId, shopId, modifyShopRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/owner/shops/{id}/event")
    public ResponseEntity<Void> createShopEvent(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("id") Integer shopId,
        @RequestBody @Valid CreateEventRequest shopEventRequest
    ) {
        ownerShopService.createEvent(ownerId, shopId, shopEventRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/owner/shops/{shopId}/events/{eventId}")
    public ResponseEntity<Void> modifyShopEvent(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("shopId") Integer shopId,
        @PathVariable("eventId") Integer eventId,
        @RequestBody @Valid ModifyEventRequest modifyEventRequest
    ) {
        ownerShopService.modifyEvent(ownerId, shopId, eventId, modifyEventRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/owner/shops/{shopId}/events/{eventId}")
    public ResponseEntity<Void> deleteShopEvent(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("shopId") Integer shopId,
        @PathVariable("eventId") Integer eventId
    ) {
        ownerShopService.deleteEvent(ownerId, shopId, eventId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/owner/shops/{shopId}/event")
    public ResponseEntity<OwnerShopEventsResponse> getShopAllEvent(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("shopId") Integer shopId
    ) {
        OwnerShopEventsResponse shopEventsResponse = ownerShopService.getShopEvent(shopId, ownerId);
        return ResponseEntity.ok(shopEventsResponse);
    }
}
