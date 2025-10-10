package in.koreatech.koin.admin.shop.repository.menu;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.MenuNotFoundException;
import in.koreatech.koin.domain.shop.model.menu.Menu;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface AdminMenuRepository extends Repository<Menu, Integer> {

    Menu save(Menu menu);

    Optional<Menu> findById(Integer menuId);

    void deleteById(Integer id);

    default Menu getById(Integer menuId) {
        return findById(menuId).orElseThrow(() -> MenuNotFoundException.withDetail("menuId: " + menuId));
    }
}
