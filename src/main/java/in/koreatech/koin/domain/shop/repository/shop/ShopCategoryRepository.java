package in.koreatech.koin.domain.shop.repository.shop;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.ShopCategoryNotFoundException;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface ShopCategoryRepository extends Repository<ShopCategory, Integer> {

    List<ShopCategory> findAll(Sort sort);

    Optional<ShopCategory> findById(Integer shopCategoryId);

    ShopCategory save(ShopCategory shopCategory);

    List<ShopCategory> findAllByIdIn(List<Integer> ids);

    default ShopCategory getById(Integer shopCategoryId) {
        return findById(shopCategoryId)
            .orElseThrow(() -> ShopCategoryNotFoundException.withDetail("shopCategoryId: " + shopCategoryId));
    }
}
