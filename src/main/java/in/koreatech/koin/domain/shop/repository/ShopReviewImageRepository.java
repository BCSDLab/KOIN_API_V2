package in.koreatech.koin.domain.shop.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.ShopReviewImage;

public interface ShopReviewImageRepository extends Repository<ShopReviewImage, Integer> {

    ShopReviewImage save(ShopReviewImage shopReviewImage);
}
