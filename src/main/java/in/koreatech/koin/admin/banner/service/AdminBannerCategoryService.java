package in.koreatech.koin.admin.banner.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.banner.dto.request.AdminBannerCategoryDescriptionModifyRequest;
import in.koreatech.koin.admin.banner.dto.response.AdminBannerCategoriesResponse;
import in.koreatech.koin.admin.banner.dto.response.AdminBannerCategoryResponse;
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

    public AdminBannerCategoryResponse modifyBannerCategoryDescription(
        AdminBannerCategoryDescriptionModifyRequest request, Integer bannerCategoryId
    ) {
        BannerCategory bannerCategory = adminBannerCategoryRepository.getById(bannerCategoryId);
        bannerCategory.modifyDescription(request.description());
        return AdminBannerCategoryResponse.of(bannerCategory);
    }
}
