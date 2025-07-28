package in.koreatech.koin.admin.banner.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.admin.banner.dto.request.AdminBannerCategoryDescriptionModifyRequest;
import in.koreatech.koin.admin.banner.dto.response.AdminBannerCategoriesResponse;
import in.koreatech.koin.admin.banner.dto.response.AdminBannerCategoryResponse;
import in.koreatech.koin.admin.banner.service.AdminBannerCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/banner-categories")
public class AdminBannerCategoryController implements AdminBannerCategoryApi {

    private final AdminBannerCategoryService adminBannerCategoryService;

    @GetMapping
    public ResponseEntity<AdminBannerCategoriesResponse> getCategories(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminBannerCategoriesResponse response = adminBannerCategoryService.getCategories();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AdminBannerCategoryResponse> modifyBannerCategoryDescription(
        @RequestBody @Valid AdminBannerCategoryDescriptionModifyRequest request,
        @PathVariable(name = "id") Integer bannerCategoryId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminBannerCategoryResponse response = adminBannerCategoryService.modifyBannerCategoryDescription(request, bannerCategoryId);
        return ResponseEntity.ok(response);
    }
}
