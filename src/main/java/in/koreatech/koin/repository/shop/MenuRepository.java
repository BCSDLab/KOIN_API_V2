package in.koreatech.koin.repository.shop;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.Menu;

public interface MenuRepository extends Repository<Menu, Long> {

    Optional<Menu> findById(Long menuId);

    Menu save(Menu menu);
}
