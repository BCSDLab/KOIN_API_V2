package in.koreatech.koin.admin.banner.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.admin.banner.dto.response.AdminBannerCategoriesResponse;
import in.koreatech.koin.admin.banner.service.AdminBannerCategoryService;
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
}
