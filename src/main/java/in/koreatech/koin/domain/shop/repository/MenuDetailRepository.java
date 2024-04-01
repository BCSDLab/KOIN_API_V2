package in.koreatech.koin.domain.shop.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.MenuOption;

public interface MenuDetailRepository extends Repository<MenuOption, Long> {

    MenuOption save(MenuOption menuOption);
}
