package in.koreatech.koin.admin.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.shop.dto.AdminCreateShopCategoryRequest;
import in.koreatech.koin.admin.shop.dto.AdminCreateShopRequest;
import in.koreatech.koin.admin.shop.dto.AdminModifyShopCategoryRequest;
import in.koreatech.koin.admin.shop.dto.AdminModifyShopRequest;
import in.koreatech.koin.admin.shop.dto.AdminShopCategoriesResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopCategoryResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopsResponse;
import in.koreatech.koin.admin.shop.service.AdminShopService;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminShopController implements AdminShopApi {

    private final AdminShopService adminShopService;

    @GetMapping("/admin/shops")
    public ResponseEntity<AdminShopsResponse> getShops(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(name = "is_deleted", defaultValue = "false") Boolean isDeleted,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminShopsResponse response = adminShopService.getShops(page, limit, isDeleted);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/shops/{id}")
    public ResponseEntity<AdminShopResponse> getShop(
        @Parameter(in = PATH) @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminShopResponse response = adminShopService.getShop(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/shops/categories")
    public ResponseEntity<AdminShopCategoriesResponse> getShopCategories(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(name = "is_deleted", defaultValue = "false") Boolean isDeleted,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminShopCategoriesResponse response = adminShopService.getShopCategories(page, limit, isDeleted);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/shops/categories/{id}")
    public ResponseEntity<AdminShopCategoryResponse> getShopCategory(
        @Parameter(in = PATH) @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminShopCategoryResponse response = adminShopService.getShopCategory(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/shops")
    public ResponseEntity<Void> createShop(
        @RequestBody AdminCreateShopRequest adminCreateShopRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.createShop(adminCreateShopRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/admin/shops/categories")
    public ResponseEntity<Void> createShopCategory(
        @RequestBody AdminCreateShopCategoryRequest adminCreateShopCategoryRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.createShopCategory(adminCreateShopCategoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/admin/shops/{id}")
    public ResponseEntity<Void> modifyShop(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody AdminModifyShopRequest adminModifyShopRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.modifyShop(id, adminModifyShopRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/shops/categories/{id}")
    public ResponseEntity<Void> modifyShopCategory(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody AdminModifyShopCategoryRequest adminModifyShopCategoryRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.modifyShopCategory(id, adminModifyShopCategoryRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/shops/{id}")
    public ResponseEntity<Void> deleteShop(
        @Parameter(in = PATH) @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.deleteShop(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/shops/categories/{id}")
    public ResponseEntity<Void> deleteShopCategory(
        @Parameter(in = PATH) @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.deleteShopCategory(id);
        return ResponseEntity.ok().build();
    }
}
