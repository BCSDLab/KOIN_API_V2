package in.koreatech.koin.admin.banner.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.admin.banner.dto.request.AdminBannerCreateRequest;
import in.koreatech.koin.admin.banner.dto.response.AdminBannerResponse;
import in.koreatech.koin.admin.banner.dto.response.AdminBannersResponse;
import in.koreatech.koin.admin.banner.service.AdminBannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/banners")
public class AdminBannerController implements AdminBannerApi {

    private final AdminBannerService adminBannerService;

    @GetMapping("/{id}")
    public ResponseEntity<AdminBannerResponse> getBanner(
        @PathVariable(name = "id") Integer bannerId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminBannerResponse response = adminBannerService.getBanner(bannerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<AdminBannersResponse> getBanners(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(name = "is_active", required = false) Boolean isActive,
        @RequestParam(name = "banner_category_name") String bannerCategoryName,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminBannersResponse response = adminBannerService.getBanners(page, limit, isActive, bannerCategoryName);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> createBanner(
        @RequestBody @Valid AdminBannerCreateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminBannerService.createBanner(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanner(
        @PathVariable(name = "id") Integer bannerId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminBannerService.deleteBanner(bannerId);
        return ResponseEntity.noContent().build();
    }
}
