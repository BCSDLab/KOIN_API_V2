package in.koreatech.koin.domain.shop.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.ShopReview;

public interface ShopReviewRepository extends Repository<ShopReview, Integer> {

    ShopReview save(ShopReview review);

    List<ShopReview> findAllByShopId(Integer shopId);
}
