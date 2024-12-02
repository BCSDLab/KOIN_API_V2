package in.koreatech.koin.admin.shop.repository.review;

import java.util.Optional;

import in.koreatech.koin.domain.shop.model.review.ShopReview;
import org.springframework.data.repository.Repository;

public interface AdminShopReviewRepository extends Repository<ShopReview, Long> {

    Optional<ShopReview> findById(Integer id);
}

