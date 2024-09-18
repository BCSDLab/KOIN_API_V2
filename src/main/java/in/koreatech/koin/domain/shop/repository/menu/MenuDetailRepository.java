package in.koreatech.koin.domain.shop.repository.menu;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.menu.MenuOption;

public interface MenuDetailRepository extends Repository<MenuOption, Integer> {

    MenuOption save(MenuOption menuOption);
}
