package in.koreatech.koin.domain.shop.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.MenuCategoryMap;

public interface MenuCategoryMapRepository extends Repository<MenuCategoryMap, Long> {

    MenuCategoryMap save(MenuCategoryMap menuCategoryMap);
}
