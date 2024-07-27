package in.koreatech.koin.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.ReviewNotFoundException;
import in.koreatech.koin.domain.shop.model.ShopReview;
import io.lettuce.core.dynamic.annotation.Param;

public interface ShopReviewRepository extends Repository<ShopReview, Integer> {

    boolean NOT_DELETED = false;

    ShopReview save(ShopReview review);

    List<ShopReview> findAllByShopIdAndIsDeleted(Integer shopId, Boolean isDeleted);

    Optional<ShopReview> findByIdAndIsDeleted(Integer reviewId, Boolean isDeleted);

    default ShopReview getByIdAndIsDeleted(Integer reviewId, Boolean isDeleted) {
        return findByIdAndIsDeleted(reviewId, isDeleted)
            .orElseThrow(() -> ReviewNotFoundException.withDetail(String.format("reviewId: %s", reviewId)));
    }

    Optional<ShopReview> findAllByIdAndShopIdAndIsDeleted(Integer reviewId, Integer shopId, Boolean isDeleted);

    default ShopReview getAllByIdAndShopIdAndIsDeleted(Integer reviewId, Integer shopId, Boolean isDeleted) {
        return findAllByIdAndShopIdAndIsDeleted(reviewId, shopId, isDeleted)
            .orElseThrow(() -> ReviewNotFoundException.withDetail(String.format("reviewId: %s", reviewId)));
    }

    @Query("""
           SELECT sr FROM ShopReview sr 
           WHERE sr.shop.id = :shopId 
           AND sr.isDeleted = :isDeleted
           AND NOT EXISTS (
               SELECT r FROM ShopReviewReport r 
               WHERE r.review.id = sr.id 
               AND r.reportStatus != in.koreatech.koin.domain.shop.model.ReportStatus.DISMISSED
           )
           """)
    Page<ShopReview> findAllByShopIdNotContainReportedAndIsDeleted(
        @Param("shopId") Integer shopId,
        @Param("isDeleted") Boolean isDeleted,
        Pageable pageable
    );

    @Query("""
       SELECT COUNT(sr) FROM ShopReview sr 
       WHERE sr.shop.id = :shopId
       AND sr.isDeleted = :isDeleted 
       AND NOT EXISTS (
           SELECT r FROM ShopReviewReport r 
           WHERE r.review.id = sr.id 
           AND r.reportStatus != in.koreatech.koin.domain.shop.model.ReportStatus.DISMISSED
       )
       """)
    Integer countByShopIdNotContainReportedAndIsDeleted(
        @Param("shopId") Integer shopId,
        @Param("isDeleted") Boolean isDeleted
        );

    @Query("""
           SELECT COUNT(sr) FROM ShopReview sr 
           WHERE sr.shop.id = :shopId 
           AND sr.isDeleted = :isDeleted
           AND NOT EXISTS (
               SELECT r FROM ShopReviewReport r 
               WHERE r.review.id = sr.id 
               AND r.reportStatus != in.koreatech.koin.domain.shop.model.ReportStatus.DISMISSED
               )
           AND sr.rating = :rating
           """)
    Integer countReviewRatingNotContainReportedAndIsDeleted(
        @Param("shopId") Integer shopId,
        @Param("isDeleted") Boolean isDeleted,
        @Param("rating") Integer rating
    );

    Optional<ShopReview> findById(Integer reviewId);
}
