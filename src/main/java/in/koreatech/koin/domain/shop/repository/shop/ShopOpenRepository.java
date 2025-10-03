package in.koreatech.koin.domain.shop.repository.shop;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.shop.ShopOpen;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface ShopOpenRepository extends Repository<ShopOpen, Integer> {

    ShopOpen save(ShopOpen shopOpen);

    List<ShopOpen> findAllByShopId(Integer shopId);
}
