package in.koreatech.koin.admin.banner.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.admin.banner.dto.response.AdminBannerResponse;
import in.koreatech.koin.admin.banner.dto.response.AdminBannersResponse;
import in.koreatech.koin.admin.banner.service.AdminBannerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminBannerController implements AdminBannerApi {

    private final AdminBannerService adminBannerService;

    @GetMapping("/admin/banner/{id}")
    public ResponseEntity<AdminBannerResponse> getBanner(
        @PathVariable(name = "id") Integer bannerId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminBannerResponse response = adminBannerService.getBanner(bannerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/banners")
    public ResponseEntity<AdminBannersResponse> getBanners(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(name = "is_active", defaultValue = "true") Boolean isActive,
        @RequestParam(name = "banner_category_name") String bannerCategoryName,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminBannersResponse response = adminBannerService.getBanners(page, limit, isActive, bannerCategoryName);
        return ResponseEntity.ok(response);
    }
}
