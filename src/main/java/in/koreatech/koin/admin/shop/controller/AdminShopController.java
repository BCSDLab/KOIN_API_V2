package in.koreatech.koin.admin.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import java.util.List;

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

import in.koreatech.koin.admin.shop.dto.shop.AdminCreateShopCategoryRequest;
import in.koreatech.koin.admin.shop.dto.shop.AdminCreateShopRequest;
import in.koreatech.koin.admin.shop.dto.shop.AdminModifyShopCategoriesOrderRequest;
import in.koreatech.koin.admin.shop.dto.shop.AdminModifyShopCategoryRequest;
import in.koreatech.koin.admin.shop.dto.shop.AdminModifyShopRequest;
import in.koreatech.koin.admin.shop.dto.shop.AdminShopCategoryResponse;
import in.koreatech.koin.admin.shop.dto.shop.AdminShopParentCategoryResponse;
import in.koreatech.koin.admin.shop.dto.shop.AdminShopResponse;
import in.koreatech.koin.admin.shop.dto.shop.AdminShopsResponse;
import in.koreatech.koin.admin.shop.service.AdminShopService;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
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
    public ResponseEntity<List<AdminShopCategoryResponse>> getShopCategories(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        List<AdminShopCategoryResponse> response = adminShopService.getShopCategories();
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

    @GetMapping("/admin/shops/parent-categories")
    public ResponseEntity<List<AdminShopParentCategoryResponse>> getShopParentCategories(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        List<AdminShopParentCategoryResponse> response = adminShopService.getShopParentCategories();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/shops")
    public ResponseEntity<Void> createShop(
        @RequestBody @Valid AdminCreateShopRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.createShop(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/admin/shops/categories")
    public ResponseEntity<Void> createShopCategory(
        @RequestBody @Valid AdminCreateShopCategoryRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.createShopCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/admin/shops/{id}")
    public ResponseEntity<Void> modifyShop(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody @Valid AdminModifyShopRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.modifyShop(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/shops/categories/{id}")
    public ResponseEntity<Void> modifyShopCategory(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody @Valid AdminModifyShopCategoryRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.modifyShopCategory(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/shops/categories/order")
    public ResponseEntity<Void> modifyShopCategoriesOrder(
        @RequestBody @Valid AdminModifyShopCategoriesOrderRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.modifyShopCategoriesOrder(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/shops/{id}/undelete")
    public ResponseEntity<Void> cancelShopDelete(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.cancelShopDelete(shopId);
        return ResponseEntity.status(HttpStatus.OK).build();
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
        return ResponseEntity.noContent().build();
    }
}
