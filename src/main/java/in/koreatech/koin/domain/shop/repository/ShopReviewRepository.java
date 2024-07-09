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

    List<ShopReview> findAllByShopId(Integer shopId);

    Optional<ShopReview> findById(Integer reviewId);

    default ShopReview getById(Integer reviewId) {
        return findById(reviewId)
            .orElseThrow(() -> ReviewNotFoundException.withDetail(String.format("reviewId: %s", reviewId)));
    }

    Optional<ShopReview> findAllByIdAndShopId(Integer reviewId, Integer shopId);

    default ShopReview getAllByIdAndShopId(Integer reviewId, Integer shopId) {
        return findAllByIdAndShopId(reviewId, shopId)
            .orElseThrow(() -> ReviewNotFoundException.withDetail(String.format("reviewId: %s", reviewId)));
    }

    void deleteById(Integer id);

    @Query("""
           SELECT sr FROM ShopReview sr 
           WHERE sr.shop.id = :shopId 
           AND NOT EXISTS (
               SELECT r FROM ShopReviewReport r 
               WHERE r.review.id = sr.id 
               AND r.reportedBy.id = :userId
           )
           """)
    Page<ShopReview> findAllByShopIdExcludingReportedByUser(@Param("shopId") Integer shopId, @Param("userId") Integer userId, Pageable pageable);

    @Query("""
           SELECT COUNT(sr) FROM ShopReview sr 
           WHERE sr.shop.id = :shopId 
           AND NOT EXISTS (
               SELECT r FROM ShopReviewReport r 
               WHERE r.review.id = sr.id 
               AND r.reportedBy.id = :userId
           )
           """)
    Integer countByShopIdExcludingReportedByUser(@Param("shopId") Integer shopId, @Param("userId") Integer userId);
}
