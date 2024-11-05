package in.koreatech.koin.domain.shop.repository.menu;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.exception.MenuNotFoundException;
import in.koreatech.koin.domain.shop.model.menu.Menu;
import org.springframework.data.repository.query.Param;

public interface MenuRepository extends Repository<Menu, Integer> {

    Optional<Menu> findById(Integer menuId);

    Menu save(Menu menu);

    void deleteById(Integer id);

    default Menu getById(Integer menuId) {
        return findById(menuId).orElseThrow(() -> MenuNotFoundException.withDetail("menuId: " + menuId));
    }

    List<Menu> findAllByShopId(Integer shopId);

    List<Menu> findAll();

    @Query("SELECT DISTINCT m.name FROM Menu m WHERE m.name LIKE :prefix%")
    List<String> findDistinctNameStartingWith(@Param("prefix") String prefix);
}
