package in.koreatech.koin.domain.shop.repository.menu;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.MenuCategoryNotFoundException;
import in.koreatech.koin.domain.shop.model.menu.MenuCategory;

public interface MenuCategoryRepository extends Repository<MenuCategory, Integer> {

    List<MenuCategory> findAllByShopId(Integer shopId);

    MenuCategory save(MenuCategory menuCategory);

    Optional<MenuCategory> findById(Integer id);

    List<MenuCategory> findAllByIdIn(List<Integer> ids);

    default MenuCategory getById(Integer id) {
        return findById(id).orElseThrow(() -> MenuCategoryNotFoundException.withDetail("categoryId: " + id));
    }

    List<MenuCategory> findByIdIn(List<Long> ids);

    Void deleteById(Integer id);
}
