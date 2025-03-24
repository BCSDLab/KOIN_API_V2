package in.koreatech.koin.domain.banner.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.banner.model.Banner;

public interface BannerRepository extends Repository<Banner, Integer> {

    List<Banner> findAllByBannerCategoryIdAndIsActiveTrueOrderByPriority(Integer categoryId);
}
