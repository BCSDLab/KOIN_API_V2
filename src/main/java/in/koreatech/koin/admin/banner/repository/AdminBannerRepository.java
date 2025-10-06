package in.koreatech.koin.admin.banner.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.admin.banner.exception.BannerNotFoundException;
import in.koreatech.koin.domain.banner.model.Banner;
import in.koreatech.koin.domain.banner.model.BannerCategory;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
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

    @Query("SELECT MAX(b.priority) from Banner b WHERE b.bannerCategory = :category AND b.isActive = true")
    Integer findMaxPriorityCategory(BannerCategory category);

    Optional<Banner> findByBannerCategoryAndPriorityAndIsActiveTrue(BannerCategory category, Integer priority);

    @Query(value = """
        SELECT * FROM banners
        WHERE banner_category_id = :bannerCategoryId
        AND is_active = :isActive
        AND priority > :priority
        """, nativeQuery = true)
    List<Banner> findLowerPriorityBannersInCategory(
        @Param("isActive") Boolean isActive,
        @Param("bannerCategoryId") Integer bannerCategoryId,
        @Param("priority") Integer priority
    );
}
