package in.koreatech.koin.domain.shop.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.MenuNotFoundException;
import in.koreatech.koin.domain.shop.exception.ShopOpenNotFoundException;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopImage;
import in.koreatech.koin.domain.shop.model.ShopOpen;

public interface ShopOpenRepository extends Repository<ShopOpen, Long> {

    ShopOpen save(ShopOpen shopOpen);

    List<ShopOpen> findAllByShopId(Long shopId);

    default List<ShopOpen> saveAll(List<ShopOpen> shopOpens) {
        List<ShopOpen> result = new ArrayList<>(shopOpens.size());
        shopOpens.forEach(shopImage -> {
            result.add(save(shopImage));
        });
        return result;
    }
}
