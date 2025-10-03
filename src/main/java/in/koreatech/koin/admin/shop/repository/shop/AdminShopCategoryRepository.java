package in.koreatech.koin.admin.shop.repository.shop;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.ShopCategoryNotFoundException;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface AdminShopCategoryRepository extends Repository<ShopCategory, Integer> {

    ShopCategory save(ShopCategory shopCategory);

    List<ShopCategory> findAll();

    List<ShopCategory> findAll(Sort sort);

    Optional<ShopCategory> findById(Integer shopCategoryId);

    List<ShopCategory> findAllByIdIn(List<Integer> ids);

    @Query("SELECT MAX(c.orderIndex) FROM ShopCategory c")
    Integer findMaxOrderIndex();

    void deleteById(Integer shopCategoryId);

    Integer count();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Integer shopCategoryId);

    default ShopCategory getById(Integer shopCategoryId) {
        return findById(shopCategoryId)
            .orElseThrow(() -> ShopCategoryNotFoundException.withDetail("shopCategoryId: " + shopCategoryId));
    }
}
