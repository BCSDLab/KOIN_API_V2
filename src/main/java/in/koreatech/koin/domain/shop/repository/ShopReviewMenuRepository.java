package in.koreatech.koin.domain.shop.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.ShopReviewMenu;

public interface ShopReviewMenuRepository extends Repository<ShopReviewMenu, Integer> {

  ShopReviewMenu save(ShopReviewMenu shopReviewMenu);
}
