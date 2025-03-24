package in.koreatech.koin.domain.banner.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.banner.exception.BannerNotFoundException;
import in.koreatech.koin.domain.banner.model.Banner;
import in.koreatech.koin.domain.banner.model.BannerCategory;

public interface BannerRepository extends Repository<Banner, Integer> {

    Optional<Banner> findById(Integer id);

    default Banner getById(Integer id) {
        return findById(id).orElseThrow(() -> BannerNotFoundException.withDetail("bannerId : " + id));
    }

    @Query("SELECT MAX(b.priority) from Banner b WHERE b.bannerCategory = :category AND b.isActive = true")
    Integer findMaxPriorityCategory(BannerCategory category);

    Optional<Banner> findByBannerCategoryAndPriorityAndIsActiveTrue(BannerCategory category, Integer priority);
}
