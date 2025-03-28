package in.koreatech.koin.domain.banner.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.banner.dto.response.BannerCategoriesResponse;
import in.koreatech.koin.domain.banner.service.BannerCategoryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/banner-categories")
public class BannerCategoryController implements BannerCategoryApi {

    private final BannerCategoryService bannerCategoryService;

    @GetMapping
    public ResponseEntity<BannerCategoriesResponse> getCategories() {
        BannerCategoriesResponse response = bannerCategoryService.getCategories();
        return ResponseEntity.ok(response);
    }
}
