package in.koreatech.koin.admin.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.MenuNotFoundException;
import in.koreatech.koin.domain.shop.model.menu.Menu;

public interface AdminMenuRepository extends Repository<Menu, Integer> {

    Optional<Menu> findById(Integer menuId);

    Menu save(Menu menu);

    void deleteById(Integer id);

    default Menu getById(Integer menuId) {
        return findById(menuId).orElseThrow(() -> MenuNotFoundException.withDetail("menuId: " + menuId));
    }

    List<Menu> findAllByShopId(Integer shopId);
}
