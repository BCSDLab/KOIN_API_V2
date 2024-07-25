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
               AND r.userId.id = :userId
           )
           """)
    Page<ShopReview> findAllByShopIdExcludingReportedByUserAndIsDeleted(
        @Param("shopId") Integer shopId,
        @Param("userId") Integer userId,
        @Param("isDeleted") Boolean isDeleted,
        Pageable pageable);

    @Query("""
           SELECT COUNT(sr) FROM ShopReview sr 
           WHERE sr.shop.id = :shopId
           AND sr.isDeleted = :isDeleted 
           AND NOT EXISTS (
               SELECT r FROM ShopReviewReport r 
               WHERE r.review.id = sr.id 
               AND r.userId.id = :userId
           )
           """)
    Integer countByShopIdExcludingReportedByUserAndIsDeleted(
        @Param("shopId") Integer shopId,
        @Param("userId") Integer userId,
        @Param("isDeleted") Boolean isDeleted
        );

    @Query("""
           SELECT COUNT(sr) FROM ShopReview sr 
           WHERE sr.shop.id = :shopId 
           AND sr.isDeleted = :isDeleted
           AND NOT EXISTS (
               SELECT r FROM ShopReviewReport r 
               WHERE r.review.id = sr.id 
               AND r.userId.id = :userId
               )
           AND sr.rating = :rating
           """)
    Integer countReviewRatingAndIsDeleted(
        @Param("shopId") Integer shopId,
        @Param("userId") Integer userId,
        @Param("isDeleted") Boolean isDeleted,
        @Param("rating") Integer rating);
}
