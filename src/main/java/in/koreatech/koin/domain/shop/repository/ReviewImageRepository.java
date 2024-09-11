package in.koreatech.koin.domain.shop.repository;


import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.review.ShopReviewImage;

public interface ReviewImageRepository extends Repository<ShopReviewImage, Integer> {

    ShopReviewImage save(ShopReviewImage reviewImage);

}
