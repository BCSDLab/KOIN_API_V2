package in.koreatech.koin.admin.benefit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import in.koreatech.koin.admin.benefit.dto.*;
import in.koreatech.koin.admin.benefit.service.AdminBenefitService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/benefit")
public class AdminBenefitController implements AdminBenefitApi {

    private final AdminBenefitService adminBenefitService;

    @GetMapping("/categories")
    public ResponseEntity<AdminBenefitCategoryResponse> getBenefitCategories() {
        AdminBenefitCategoryResponse response = adminBenefitService.getBenefitCategories();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/categories")
    public ResponseEntity<AdminCreateBenefitCategoryResponse> createBenefitCategory(
        @RequestBody AdminCreateBenefitCategoryRequest request
    ) {
        AdminCreateBenefitCategoryResponse response = adminBenefitService.createBenefitCategory(request);
        return ResponseEntity.status(201).body(response);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteBenefitCategory(
        @PathVariable("id") Integer categoryId
    ) {
        adminBenefitService.deleteBenefitCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/shops")
    public ResponseEntity<AdminBenefitShopsResponse> getBenefitShops(
        @PathVariable("id") Integer benefitId) {
        AdminBenefitShopsResponse response = adminBenefitService.getBenefitShops(benefitId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/shops")
    public ResponseEntity<AdminCreateBenefitShopsResponse> createBenefitShops(
        @PathVariable("id") Integer benefitId,
        @RequestBody AdminCreateBenefitShopsRequest request
    ) {
        AdminCreateBenefitShopsResponse response = adminBenefitService.createBenefitShops(benefitId, request);
        return ResponseEntity.status(201).body(response);
    }

    @DeleteMapping("/{id}/shops")
    public ResponseEntity<Void> deleteBenefitShops(
        @PathVariable("id") Integer benefitId,
        @RequestBody AdminDeleteShopsRequest request
    ) {
        adminBenefitService.deleteBenefitShops(benefitId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/shops/search")
    public ResponseEntity<AdminSearchBenefitShopsResponse> searchShops(
        @PathVariable("id") Integer benefitId,
        @RequestParam("search_keyword") String searchKeyword
    ) {
        AdminSearchBenefitShopsResponse response = adminBenefitService.searchShops(benefitId, searchKeyword);
        return ResponseEntity.ok(response);
    }
}
