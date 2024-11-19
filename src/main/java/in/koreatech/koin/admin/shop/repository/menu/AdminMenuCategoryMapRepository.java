package in.koreatech.koin.admin.shop.repository.menu;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.menu.MenuCategoryMap;

public interface AdminMenuCategoryMapRepository extends Repository<MenuCategoryMap, Integer> {

    MenuCategoryMap save(MenuCategoryMap menuCategoryMap);
}
