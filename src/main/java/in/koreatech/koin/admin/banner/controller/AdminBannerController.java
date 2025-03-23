package in.koreatech.koin.admin.banner.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.admin.banner.dto.response.AdminBannerResponse;
import in.koreatech.koin.admin.banner.service.AdminBannerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/banner")
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
}
