package in.koreatech.koin.domain.shop.repository.review;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.review.ShopReviewImage;
import in.koreatech.koin.config.repository.JpaRepository;

@JpaRepository
public interface ShopReviewImageRepository extends Repository<ShopReviewImage, Integer> {

    ShopReviewImage save(ShopReviewImage shopReviewImage);
}
