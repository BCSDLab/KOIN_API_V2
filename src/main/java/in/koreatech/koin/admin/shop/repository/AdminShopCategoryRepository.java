package in.koreatech.koin.admin.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.ShopCategoryNotFoundException;
import in.koreatech.koin.domain.shop.model.ShopCategory;

public interface AdminShopCategoryRepository extends Repository<ShopCategory, Integer> {

    Page<ShopCategory> findAllByIsDeleted(boolean isDeleted, Pageable pageable);

    Integer countAllByIsDeleted(boolean isDeleted);

    Optional<ShopCategory> findById(Integer shopCategoryId);

    ShopCategory save(ShopCategory shopCategory);

    List<ShopCategory> findAllByIdIn(List<Integer> ids);

    Optional<ShopCategory> findByName(String name);

    List<ShopCategory> findAll();

    default ShopCategory getById(Integer shopCategoryId) {
        return findById(shopCategoryId)
            .orElseThrow(() -> ShopCategoryNotFoundException.withDetail("shopCategoryId: " + shopCategoryId));
    }
}
