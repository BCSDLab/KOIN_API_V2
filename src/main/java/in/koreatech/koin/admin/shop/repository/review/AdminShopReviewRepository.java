package in.koreatech.koin.admin.shop.repository.review;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.review.ShopReview;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface AdminShopReviewRepository extends Repository<ShopReview, Long> {

    Optional<ShopReview> findById(Integer id);
}

