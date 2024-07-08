package in.koreatech.koin.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.ReviewNotFoundException;
import in.koreatech.koin.domain.shop.model.ShopReview;

public interface ShopReviewRepository extends Repository<ShopReview, Integer> {

    ShopReview save(ShopReview review);

    List<ShopReview> findAllByShopId(Integer shopId);

    Optional<ShopReview> findByShopIdAndReviewerId(Integer shopId, Integer reviewerId);

    default ShopReview getByShopIdAndReviewerId(Integer shopId, Integer reviewerId) {
        return findByShopIdAndReviewerId(shopId, reviewerId)
            .orElseThrow(() -> ReviewNotFoundException.withDetail(String.format("shopId, reviewerId: %s, %s", shopId, reviewerId)));
    }

    void deleteById(Integer id);

    ShopReview getById(Integer integer);
}
