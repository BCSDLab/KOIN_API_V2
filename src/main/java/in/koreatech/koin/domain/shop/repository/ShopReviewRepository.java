package in.koreatech.koin.domain.shop.repository;

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

    @Query("""
           SELECT sr FROM ShopReview sr 
           WHERE sr.shop.id = :shopId 
           AND sr.isDeleted = false
           AND NOT EXISTS (
               SELECT r FROM ShopReviewReport r 
               WHERE r.review.id = sr.id 
               AND r.reportStatus != in.koreatech.koin.domain.shop.model.ReportStatus.DISMISSED
           )
           """)
    Page<ShopReview> findAllByShopIdNotContainReportedAndIsDeletedFalse(
        @Param("shopId") Integer shopId,
        Pageable pageable
    );

    @Query("""
       SELECT COUNT(sr) FROM ShopReview sr 
       WHERE sr.shop.id = :shopId
       AND sr.isDeleted = false 
       AND NOT EXISTS (
           SELECT r FROM ShopReviewReport r 
           WHERE r.review.id = sr.id 
           AND r.reportStatus != 'DISMISSED'
       )
       """)
    Integer countByShopIdNotContainReportedAndIsDeletedFalse(
        @Param("shopId") Integer shopId
        );

    @Query("""
           SELECT COUNT(sr) FROM ShopReview sr 
           WHERE sr.shop.id = :shopId 
           AND sr.isDeleted = false
           AND NOT EXISTS (
               SELECT r FROM ShopReviewReport r 
               WHERE r.review.id = sr.id 
               AND r.reportStatus != 'DISMISSED'
               )
           AND sr.rating = :rating
           """)
    Integer countReviewRatingNotContainReportedAndIsDeletedFalse(
        @Param("shopId") Integer shopId,
        @Param("rating") Integer rating
    );

    Optional<ShopReview> findById(Integer reviewId);
}
