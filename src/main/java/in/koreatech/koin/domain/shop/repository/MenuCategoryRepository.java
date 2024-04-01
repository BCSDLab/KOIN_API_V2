package in.koreatech.koin.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.MenuCategoryNotFoundException;
import in.koreatech.koin.domain.shop.model.MenuCategory;

public interface MenuCategoryRepository extends Repository<MenuCategory, Long> {

    List<MenuCategory> findAllByShopId(Long shopId);

    MenuCategory save(MenuCategory menuCategory);

    Optional<MenuCategory> findById(Long id);

    List<MenuCategory> findAllByIdIn(List<Long> ids);

    default MenuCategory getById(Long id) {
        return findById(id).orElseThrow(() -> MenuCategoryNotFoundException.withDetail("categoryId: " + id));
    }

    Void deleteById(Long id);
}
