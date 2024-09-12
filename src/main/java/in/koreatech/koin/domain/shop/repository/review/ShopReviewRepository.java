package in.koreatech.koin.domain.shop.repository.review;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.ReviewNotFoundException;
import in.koreatech.koin.domain.shop.model.review.ShopReview;

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
}
