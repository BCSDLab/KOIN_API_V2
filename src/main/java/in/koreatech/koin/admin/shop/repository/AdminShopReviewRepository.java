package in.koreatech.koin.admin.shop.repository;

import in.koreatech.koin.domain.shop.model.ShopReview;
import org.springframework.data.repository.Repository;

public interface AdminShopReviewRepository extends Repository<ShopReview, Long> {
    ShopReview findById(Integer id);
}
