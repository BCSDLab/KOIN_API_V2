package in.koreatech.koin.admin.banner.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.admin.banner.exception.BannerNotFoundException;
import in.koreatech.koin.domain.banner.model.Banner;

public interface AdminBannerRepository extends Repository<Banner, Integer> {

    Optional<Banner> findById(Integer id);

    default Banner getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> BannerNotFoundException.withDetail("banner id : " + id));
    }

    Banner save(Banner banner);

    @Query(value = """
        SELECT COUNT(*) FROM banners
        WHERE is_active = :isActive
        AND banner_category_id = :bannerCategoryId
        """, nativeQuery = true)
    Integer countByIsActiveAndBannerCategoryId(@Param("isActive") boolean isActive,
        @Param("bannerCategoryId") Integer bannerCategoryId);

    @Query(value = """
        SELECT COUNT(*) FROM banners
        WHERE banner_category_id = :bannerCategoryId
        """, nativeQuery = true)
    Integer countByBannerCategoryId(@Param("bannerCategoryId") Integer bannerCategoryId);

    @Query(value = """
        SELECT * FROM banners
        WHERE is_active = :isActive
        AND banner_category_id = :bannerCategoryId
        """, nativeQuery = true)
    Page<Banner> findAllByIsActiveAndBannerCategoryId(@Param("isActive") boolean isActive,
        @Param("bannerCategoryId") Integer bannerCategoryId, Pageable pageable);

    @Query(value = """
        SELECT * FROM banners
        WHERE banner_category_id = :bannerCategoryId
        """, nativeQuery = true)
    Page<Banner> findAllByBannerCategoryId(@Param("bannerCategoryId") Integer bannerCategoryId, Pageable pageable);

    void deleteById(Integer id);
}
