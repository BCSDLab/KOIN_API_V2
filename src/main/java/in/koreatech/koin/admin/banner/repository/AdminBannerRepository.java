package in.koreatech.koin.admin.banner.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.banner.exception.BannerNotFoundException;
import in.koreatech.koin.domain.banner.model.Banner;

public interface AdminBannerRepository extends Repository<Banner, Integer> {

    Optional<Banner> findById(Integer id);

    default Banner getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> BannerNotFoundException.withDetail("banner id : " + id));
    }
}
