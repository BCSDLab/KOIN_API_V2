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

import in.koreatech.koin.admin.shop.dto.AdminCreateMenuCategoryRequest;
import in.koreatech.koin.admin.shop.dto.AdminCreateMenuRequest;
import in.koreatech.koin.admin.shop.dto.AdminCreateShopCategoryRequest;
import in.koreatech.koin.admin.shop.dto.AdminCreateShopRequest;
import in.koreatech.koin.admin.shop.dto.AdminMenuCategoriesResponse;
import in.koreatech.koin.admin.shop.dto.AdminMenuDetailResponse;
import in.koreatech.koin.admin.shop.dto.AdminModifyMenuCategoryRequest;
import in.koreatech.koin.admin.shop.dto.AdminModifyMenuRequest;
import in.koreatech.koin.admin.shop.dto.AdminModifyShopCategoryRequest;
import in.koreatech.koin.admin.shop.dto.AdminModifyShopRequest;
import in.koreatech.koin.admin.shop.dto.AdminModifyShopReviewReportStatusRequest;
import in.koreatech.koin.admin.shop.dto.AdminShopCategoryResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopMenuResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopsResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopsReviewsResponse;
import in.koreatech.koin.admin.shop.service.AdminShopService;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
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

    @GetMapping("/admin/shops/{id}/menus")
    public ResponseEntity<AdminShopMenuResponse> getAllMenus(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminShopMenuResponse adminShopMenuResponse = adminShopService.getAllMenus(shopId);
        return ResponseEntity.ok(adminShopMenuResponse);
    }

    @GetMapping("/admin/shops/{id}/menus/categories")
    public ResponseEntity<AdminMenuCategoriesResponse> getAllMenuCategories(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminMenuCategoriesResponse adminMenuCategoriesResponse = adminShopService.getAllMenuCategories(shopId);
        return ResponseEntity.ok(adminMenuCategoriesResponse);
    }

    @GetMapping("/admin/shops/{shopId}/menus/{menuId}")
    public ResponseEntity<AdminMenuDetailResponse> getMenu(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @Parameter(in = PATH) @PathVariable("menuId") Integer menuId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminMenuDetailResponse adminMenuDetailResponse = adminShopService.getMenu(shopId, menuId);
        return ResponseEntity.ok(adminMenuDetailResponse);
    }

    @PostMapping("/admin/shops")
    public ResponseEntity<Void> createShop(
        @RequestBody @Valid AdminCreateShopRequest adminCreateShopRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.createShop(adminCreateShopRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/admin/shops/categories")
    public ResponseEntity<Void> createShopCategory(
        @RequestBody @Valid AdminCreateShopCategoryRequest adminCreateShopCategoryRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.createShopCategory(adminCreateShopCategoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/admin/shops/{id}")
    public ResponseEntity<Void> modifyShop(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody @Valid AdminModifyShopRequest adminModifyShopRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.modifyShop(id, adminModifyShopRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/shops/categories/{id}")
    public ResponseEntity<Void> modifyShopCategory(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody @Valid AdminModifyShopCategoryRequest adminModifyShopCategoryRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.modifyShopCategory(id, adminModifyShopCategoryRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/shops/categories/order")
    public ResponseEntity<Void> modifyShopCategoriesOrder(
        @RequestBody @Valid List<Integer> shopCategoryIds,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.modifyShopCategoriesOrder(shopCategoryIds);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/shops/{shopId}/menus/categories")
    public ResponseEntity<Void> modifyMenuCategory(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @RequestBody AdminModifyMenuCategoryRequest adminModifyMenuCategoryRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.modifyMenuCategory(shopId, adminModifyMenuCategoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/admin/shops/{shopId}/menus/{menuId}")
    public ResponseEntity<Void> modifyMenu(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @Parameter(in = PATH) @PathVariable("menuId") Integer menuId,
        @RequestBody @Valid AdminModifyMenuRequest adminModifyMenuRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.modifyMenu(shopId, menuId, adminModifyMenuRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/admin/shops/{id}/menus")
    public ResponseEntity<Void> createMenu(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
        @RequestBody @Valid AdminCreateMenuRequest adminCreateMenuRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.createMenu(shopId, adminCreateMenuRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/admin/shops/{id}/menus/categories")
    public ResponseEntity<Void> createMenuCategory(
        @Parameter(in = PATH) @PathVariable("id") Integer shopId,
        @RequestBody @Valid AdminCreateMenuCategoryRequest adminCreateMenuCategoryRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.createMenuCategory(shopId, adminCreateMenuCategoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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

    @DeleteMapping("/admin/shops/{shopId}/menus/categories/{categoryId}")
    public ResponseEntity<Void> deleteMenuCategory(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @Parameter(in = PATH) @PathVariable("categoryId") Integer categoryId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.deleteMenuCategory(shopId, categoryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/admin/shops/{shopId}/menus/{menuId}")
    public ResponseEntity<Void> deleteMenu(
        @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
        @Parameter(in = PATH) @PathVariable("menuId") Integer menuId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.deleteMenu(shopId, menuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/admin/shops/reviews")
    public ResponseEntity<AdminShopsReviewsResponse> getReviews(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(name = "is_reported", required = false) Boolean isReported,
        @RequestParam(name = "has_unhandled_report", required = false) Boolean hasUnhandledReport,
        @RequestParam(name = "shop_id", required = false) Integer shopId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(adminShopService.getReviews(
                page, limit, isReported, hasUnhandledReport, shopId
            ));
    }

    @Operation(summary = "리뷰 신고 상태 변경")
    @PutMapping("/admin/shops/reviews/{id}")
    public ResponseEntity<Void> modifyReviewReportStatus(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody @Valid AdminModifyShopReviewReportStatusRequest adminModifyShopReviewReportStatusRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.modifyShopReviewReportStatus(id, adminModifyShopReviewReportStatusRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "리뷰 삭제")
    @DeleteMapping("/admin/shops/reviews/{id}")
    public ResponseEntity<Void> deleteReview(
        @Parameter(in = PATH) @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShopService.deleteShopReview(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
