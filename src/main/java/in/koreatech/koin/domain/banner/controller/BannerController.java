package in.koreatech.koin.domain.banner.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.banner.dto.response.BannersResponse;
import in.koreatech.koin.domain.banner.enums.PlatformType;
import in.koreatech.koin.domain.banner.service.BannerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/banners")
public class BannerController implements BannerApi {

    private final BannerService bannerService;

    @GetMapping("/{categoryId}")
    public ResponseEntity<BannersResponse> getBannersByCategoryAndPlatform(
        @PathVariable Integer categoryId,
        @RequestParam PlatformType platform
    ) {
        BannersResponse response = bannerService.getBannerByCategoryAndPlatform(categoryId, platform);
        return ResponseEntity.ok(response);
    }
}
