package in.koreatech.koin.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.MenuCategoryMap;

public interface MenuCategoryMapRepository extends Repository<MenuCategoryMap, Long> {
    MenuCategoryMap save(MenuCategoryMap menuCategoryMap);
}
