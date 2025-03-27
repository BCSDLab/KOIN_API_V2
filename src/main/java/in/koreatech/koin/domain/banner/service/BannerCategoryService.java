package in.koreatech.koin.domain.banner.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.banner.dto.response.BannerCategoriesResponse;
import in.koreatech.koin.domain.banner.model.BannerCategory;
import in.koreatech.koin.domain.banner.repository.BannerCategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerCategoryService {

    private final BannerCategoryRepository bannerCategoryRepository;

    public BannerCategoriesResponse getCategories() {
        List<BannerCategory> bannerCategories = bannerCategoryRepository.findAll();
        return BannerCategoriesResponse.of(bannerCategories);
    }
}
