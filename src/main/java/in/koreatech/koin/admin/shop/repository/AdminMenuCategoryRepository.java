package in.koreatech.koin.admin.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.MenuCategoryNotFoundException;
import in.koreatech.koin.domain.shop.model.menu.MenuCategory;

public interface AdminMenuCategoryRepository extends Repository<MenuCategory, Integer> {

    MenuCategory save(MenuCategory menuCategory);

    List<MenuCategory> findAllByShopId(Integer shopId);

    Optional<MenuCategory> findById(Integer id);

    List<MenuCategory> findAllByIdIn(List<Integer> ids);

    Void deleteById(Integer id);

    default MenuCategory getById(Integer id) {
        return findById(id).orElseThrow(() -> MenuCategoryNotFoundException.withDetail("categoryId: " + id));
    }
}
