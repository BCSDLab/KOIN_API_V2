package in.koreatech.koin.domain.banner.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.banner.model.BannerCategory;

public interface BannerCategoryRepository extends Repository<BannerCategory, Integer> {
    List<BannerCategory> findAll();
}
