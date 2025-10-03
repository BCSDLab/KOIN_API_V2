package in.koreatech.koin.domain.shop.repository.review;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.review.ShopReviewMenu;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface ShopReviewMenuRepository extends Repository<ShopReviewMenu, Integer> {

  ShopReviewMenu save(ShopReviewMenu shopReviewMenu);
}
