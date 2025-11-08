package in.koreatech.koin.admin.benefit.controller;

import static in.koreatech.koin.admin.history.enums.DomainType.BENEFIT;
import static in.koreatech.koin.admin.history.enums.DomainType.BENEFIT_CATEGORIES;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.benefit.dto.AdminBenefitCategoriesResponse;
import in.koreatech.koin.admin.benefit.dto.AdminBenefitShopsResponse;
import in.koreatech.koin.admin.benefit.dto.AdminCreateBenefitCategoryRequest;
import in.koreatech.koin.admin.benefit.dto.AdminCreateBenefitCategoryResponse;
import in.koreatech.koin.admin.benefit.dto.AdminCreateBenefitShopsRequest;
import in.koreatech.koin.admin.benefit.dto.AdminCreateBenefitShopsResponse;
import in.koreatech.koin.admin.benefit.dto.AdminDeleteShopsRequest;
import in.koreatech.koin.admin.benefit.dto.AdminModifyBenefitCategoryRequest;
import in.koreatech.koin.admin.benefit.dto.AdminModifyBenefitCategoryResponse;
import in.koreatech.koin.admin.benefit.dto.AdminModifyBenefitShopsRequest;
import in.koreatech.koin.admin.benefit.dto.AdminSearchBenefitShopsResponse;
import in.koreatech.koin.admin.benefit.service.AdminBenefitService;
import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/benefit")
public class AdminBenefitController implements AdminBenefitApi {

    private final AdminBenefitService adminBenefitService;

    @GetMapping("/categories")
    public ResponseEntity<AdminBenefitCategoriesResponse> getBenefitCategories(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminBenefitCategoriesResponse response = adminBenefitService.getBenefitCategories();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/categories")
    @AdminActivityLogging(domain = BENEFIT_CATEGORIES)
    public ResponseEntity<AdminCreateBenefitCategoryResponse> createBenefitCategory(
        @RequestBody AdminCreateBenefitCategoryRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminCreateBenefitCategoryResponse response = adminBenefitService.createBenefitCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/categories/{id}")
    @AdminActivityLogging(domain = BENEFIT_CATEGORIES, domainIdParam = "categoryId")
    public ResponseEntity<AdminModifyBenefitCategoryResponse> modifyBenefitCategory(
        @PathVariable("id") Integer categoryId,
        @RequestBody AdminModifyBenefitCategoryRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminModifyBenefitCategoryResponse response = adminBenefitService.modifyBenefitCategory(categoryId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/categories/{id}")
    @AdminActivityLogging(domain = BENEFIT_CATEGORIES, domainIdParam = "categoryId")
    public ResponseEntity<Void> deleteBenefitCategory(
        @PathVariable("id") Integer categoryId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminBenefitService.deleteBenefitCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/shops")
    public ResponseEntity<AdminBenefitShopsResponse> getBenefitShops(
        @PathVariable("id") Integer benefitId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminBenefitShopsResponse response = adminBenefitService.getBenefitShops(benefitId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/shops")
    @AdminActivityLogging(domain = BENEFIT, domainIdParam = "benefitId")
    public ResponseEntity<AdminCreateBenefitShopsResponse> createBenefitShops(
        @PathVariable("id") Integer benefitId,
        @RequestBody AdminCreateBenefitShopsRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminCreateBenefitShopsResponse response = adminBenefitService.createBenefitShops(benefitId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    @AdminActivityLogging(domain = BENEFIT)
    public ResponseEntity<Void> modifyBenefitShops(
        @RequestBody AdminModifyBenefitShopsRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminBenefitService.modifyBenefitShops(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/shops")
    @AdminActivityLogging(domain = BENEFIT, domainIdParam = "benefitId")
    public ResponseEntity<Void> deleteBenefitShops(
        @PathVariable("id") Integer benefitId,
        @RequestBody AdminDeleteShopsRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminBenefitService.deleteBenefitShops(benefitId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/shops/search")
    public ResponseEntity<AdminSearchBenefitShopsResponse> searchShops(
        @PathVariable("id") Integer benefitId,
        @RequestParam("search_keyword") String searchKeyword,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminSearchBenefitShopsResponse response = adminBenefitService.searchShops(benefitId, searchKeyword);
        return ResponseEntity.ok(response);
    }
}
