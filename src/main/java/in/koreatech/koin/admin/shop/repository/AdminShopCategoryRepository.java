package in.koreatech.koin.admin.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.shop.exception.ShopCategoryNotFoundException;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;

public interface AdminShopCategoryRepository extends Repository<ShopCategory, Integer> {

    List<ShopCategory> findAll(Sort sort);

    @Query(value = "SELECT * FROM shop_categories WHERE id = :shopCategoryId", nativeQuery = true)
    Optional<ShopCategory> findById(@Param("shopCategoryId") Integer shopCategoryId);

    List<ShopCategory> findAllByIdIn(List<Integer> ids);

    Optional<ShopCategory> findByName(String name);

    ShopCategory save(ShopCategory shopCategory);

    default ShopCategory getById(Integer shopCategoryId) {
        return findById(shopCategoryId)
            .orElseThrow(() -> ShopCategoryNotFoundException.withDetail("shopCategoryId: " + shopCategoryId));
    }

    Integer count();

    void deleteById(Integer shopCategoryId);
}
