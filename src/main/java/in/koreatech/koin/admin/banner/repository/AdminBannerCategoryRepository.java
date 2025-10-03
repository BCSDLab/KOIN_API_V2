package in.koreatech.koin.admin.banner.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.banner.exception.BannerCategoryNotFoundException;
import in.koreatech.koin.domain.banner.model.BannerCategory;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface AdminBannerCategoryRepository extends Repository<BannerCategory, Integer> {

    Optional<BannerCategory> findByName(String name);

    default BannerCategory getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> BannerCategoryNotFoundException.withDetail("name : " + name));
    }

    Optional<BannerCategory> findById(Integer id);

    default BannerCategory getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> BannerCategoryNotFoundException.withDetail("id : " + id));
    }

    BannerCategory save(BannerCategory bannerCategory);

    List<BannerCategory> findAll();
}
