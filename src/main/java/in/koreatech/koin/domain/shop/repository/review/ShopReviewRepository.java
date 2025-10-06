package in.koreatech.koin.domain.shop.repository.review;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.shop.exception.ReviewNotFoundException;
import in.koreatech.koin.domain.shop.model.review.ShopReview;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface ShopReviewRepository extends Repository<ShopReview, Integer> {

    ShopReview save(ShopReview review);

    Optional<ShopReview> findByIdAndIsDeletedFalse(Integer reviewId);

    default ShopReview getByIdAndIsDeleted(Integer reviewId) {
        return findByIdAndIsDeletedFalse(reviewId)
            .orElseThrow(() -> ReviewNotFoundException.withDetail(String.format("reviewId: %s", reviewId)));
    }

    Optional<ShopReview> findAllByIdAndShopIdAndIsDeletedFalse(Integer reviewId, Integer shopId);

    default ShopReview getAllByIdAndShopIdAndIsDeleted(Integer reviewId, Integer shopId) {
        return findAllByIdAndShopIdAndIsDeletedFalse(reviewId, shopId)
            .orElseThrow(() -> ReviewNotFoundException.withDetail(String.format("reviewId: %s", reviewId)));
    }

    List<ShopReview> findByShopIdAndReviewerIdAndIsDeletedFalse(Integer shopId, Integer studentId, Sort sort);

    Page<ShopReview> findByShopIdAndIsDeletedFalse(Integer shopId, Pageable pageable);

    Integer countByShopIdAndIsDeletedFalse(Integer shopId);

    Integer countByShopIdAndRatingAndIsDeletedFalse(Integer shopId, Integer rating);

    Optional<ShopReview> findById(Integer reviewId);

    @Query(value = "SELECT * FROM shop_reviews "
        + "WHERE reviewer_id = :studentId "
        + "AND shop_id = :shopId "
        + "AND created_at > :currentTime - INTERVAL 1 DAY "
        + "ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    Optional<ShopReview> findLatestReviewByStudentIdAndShopIdWithin24Hours(
        @Param("studentId") Integer studentId,
        @Param("shopId") Integer shopId,
        @Param("currentTime") LocalDateTime currentTime
    );
}
