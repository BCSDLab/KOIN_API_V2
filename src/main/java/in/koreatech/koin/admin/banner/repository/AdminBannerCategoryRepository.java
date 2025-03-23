package in.koreatech.koin.admin.banner.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.banner.exception.BannerCategoryNotFoundException;
import in.koreatech.koin.domain.banner.model.BannerCategory;

public interface AdminBannerCategoryRepository extends Repository<BannerCategory, Integer> {

    Optional<BannerCategory> findByName(String name);

    default BannerCategory getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> BannerCategoryNotFoundException.withDetail("name : " + name));
    }

    BannerCategory save(BannerCategory bannerCategory);
}
