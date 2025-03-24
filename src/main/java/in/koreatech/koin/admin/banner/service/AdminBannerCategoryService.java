package in.koreatech.koin.admin.banner.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.banner.dto.response.AdminBannerCategoriesResponse;
import in.koreatech.koin.admin.banner.repository.AdminBannerCategoryRepository;
import in.koreatech.koin.domain.banner.model.BannerCategory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBannerCategoryService {

    private final AdminBannerCategoryRepository adminBannerCategoryRepository;

    public AdminBannerCategoriesResponse getCategories() {
        List<BannerCategory> bannerCategories = adminBannerCategoryRepository.findAll();
        return AdminBannerCategoriesResponse.of(bannerCategories);
    }
}
