package in.koreatech.koin.admin.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.shop.dto.AdminCreateShopRequest;
import in.koreatech.koin.admin.shop.dto.AdminCreateShopsCategoryRequest;
import in.koreatech.koin.admin.shop.dto.AdminModifyShopRequest;
import in.koreatech.koin.admin.shop.dto.AdminModifyShopsCategoryRequest;
import in.koreatech.koin.admin.shop.dto.AdminShopResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopsCategoriesResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopsCategoryResponse;
import in.koreatech.koin.admin.shop.dto.AdminShopsResponse;
import in.koreatech.koin.admin.shop.service.AdminShopService;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminShopController implements AdminShopApi {

    private final AdminShopService adminShopService;

    @GetMapping("/shops")
    public ResponseEntity<AdminShopsResponse> getShops(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return null;
    }

    @GetMapping("/shops/{id}")
    public ResponseEntity<AdminShopResponse> getShop(
        @Parameter(in = PATH) @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return null;
    }

    @GetMapping("/shops/categories")
    public ResponseEntity<AdminShopsCategoriesResponse> getShopsCategories(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return null;
    }

    @GetMapping("/shops/categories/{id}")
    public ResponseEntity<AdminShopsCategoryResponse> getShopsCategory(
        @Parameter(in = PATH) @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return null;
    }

    @PostMapping("/shops")
    public ResponseEntity<Void> createShop(
        AdminCreateShopRequest adminCreateShopRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return null;
    }

    @PostMapping("/shops/categories")
    public ResponseEntity<Void> createShopsCategory(
        AdminCreateShopsCategoryRequest adminCreateShopsCategoryRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return null;
    }

    @PutMapping("/shops/{id}")
    public ResponseEntity<Void> modifyShop(
        @Parameter(in = PATH) @PathVariable Integer id,
        AdminModifyShopRequest adminModifyShopRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return null;
    }

    @PutMapping("/shops/categories/{id}")
    public ResponseEntity<Void> modifyShopsCategory(
        @Parameter(in = PATH) @PathVariable Integer id,
        AdminModifyShopsCategoryRequest adminModifyShopsCategoryRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return null;
    }

    @DeleteMapping("/shops/{id}")
    public ResponseEntity<Void> deleteShop(
        @Parameter(in = PATH) @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return null;
    }

    @DeleteMapping("/shops/categories/{id}")
    public ResponseEntity<Void> deleteShopsCategory(
        @Parameter(in = PATH) @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return null;
    }
}
