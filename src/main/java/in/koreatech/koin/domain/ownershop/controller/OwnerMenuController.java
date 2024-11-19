package in.koreatech.koin.domain.ownershop.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import in.koreatech.koin.domain.ownershop.service.OwnerMenuService;
import in.koreatech.koin.domain.shop.dto.menu.request.CreateCategoryRequest;
import in.koreatech.koin.domain.shop.dto.menu.request.CreateMenuRequest;
import in.koreatech.koin.domain.shop.dto.menu.request.ModifyCategoryRequest;
import in.koreatech.koin.domain.shop.dto.menu.request.ModifyMenuRequest;
import in.koreatech.koin.domain.shop.dto.menu.response.MenuCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.menu.response.MenuDetailResponse;
import in.koreatech.koin.domain.shop.dto.menu.response.ShopMenuResponse;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@Controller
@RequiredArgsConstructor
public class OwnerMenuController implements OwnerMenuApi {

    private final OwnerMenuService ownerMenuService;

    @GetMapping("/owner/shops/menus/{menuId}")
    public ResponseEntity<MenuDetailResponse> getMenuByMenuId(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable Integer menuId
    ) {
        MenuDetailResponse menuDetailResponse = ownerMenuService.getMenuByMenuId(ownerId, menuId);
        return ResponseEntity.ok(menuDetailResponse);
    }

    @GetMapping("/owner/shops/menus")
    public ResponseEntity<ShopMenuResponse> getMenus(
        @Auth(permit = {OWNER}) Integer ownerId,
        @RequestParam("shopId") Integer shopId
    ) {
        ShopMenuResponse shopMenuResponse = ownerMenuService.getMenus(shopId, ownerId);
        return ResponseEntity.ok(shopMenuResponse);
    }

    @GetMapping("/owner/shops/menus/categories")
    public ResponseEntity<MenuCategoriesResponse> getCategories(
        @Auth(permit = {OWNER}) Integer ownerId,
        @RequestParam("shopId") Integer shopId
    ) {
        MenuCategoriesResponse menuCategoriesResponse = ownerMenuService.getCategories(shopId, ownerId);
        return ResponseEntity.ok(menuCategoriesResponse);
    }

    @DeleteMapping("/owner/shops/menus/{menuId}")
    public ResponseEntity<Void> deleteMenuByMenuId(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("menuId") Integer menuId
    ) {
        ownerMenuService.deleteMenuByMenuId(ownerId, menuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/owner/shops/menus/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("categoryId") Integer categoryId
    ) {
        ownerMenuService.deleteCategory(ownerId, categoryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/owner/shops/{id}/menus")
    public ResponseEntity<Void> createMenu(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("id") Integer shopId,
        @RequestBody @Valid CreateMenuRequest createMenuRequest
    ) {
        ownerMenuService.createMenu(shopId, ownerId, createMenuRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/owner/shops/{id}/menus/categories")
    public ResponseEntity<Void> createMenuCategory(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("id") Integer shopId,
        @RequestBody @Valid CreateCategoryRequest createCategoryRequest
    ) {
        ownerMenuService.createMenuCategory(shopId, ownerId, createCategoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/owner/shops/menus/{menuId}")
    public ResponseEntity<Void> modifyMenu(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("menuId") Integer menuId,
        @RequestBody @Valid ModifyMenuRequest modifyMenuRequest
    ) {
        ownerMenuService.modifyMenu(ownerId, menuId, modifyMenuRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/owner/shops/menus/categories/{categoryId}")
    public ResponseEntity<Void> modifyMenuCategory(
        @Auth(permit = {OWNER}) Integer ownerId,
        @PathVariable("categoryId") Integer categoryId,
        @RequestBody @Valid ModifyCategoryRequest modifyCategoryRequest
    ) {
        ownerMenuService.modifyCategory(ownerId, categoryId, modifyCategoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
